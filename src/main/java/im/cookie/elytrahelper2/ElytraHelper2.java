package im.cookie.elytrahelper2;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ElytraHelper2 implements ClientModInitializer {

    public static KeyBinding fireworkKey;
    private boolean fireworkUsed = false;
    private long lastFireworkTime = 0;

    @Override
    public void onInitializeClient() {
        // Регистрация кнопки в настройках управления
        fireworkKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elytrahelper.firework",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.elytrahelper.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Проверка нажатия кнопки фейерверка
            if (fireworkKey.wasPressed()) {
                if (System.currentTimeMillis() - lastFireworkTime > 200) {
                    fireworkUsed = true;
                    lastFireworkTime = System.currentTimeMillis();
                }
            }

            // Обработка использования фейерверка
            if (fireworkUsed) {
                handleFireworkUse(client);
                fireworkUsed = false;
            }
        });
    }

    private void handleFireworkUse(net.minecraft.client.MinecraftClient client) {
        if (client.player == null) return;

        // Проверяем, что надет элитры
        ItemStack chestSlot = client.player.getInventory().getArmorStack(2);
        if (!chestSlot.isOf(Items.ELYTRA)) {
            client.player.sendMessage(Text.literal("Элитры не надеты!"), true);
            return;
        }

        // Проверяем, что игрок летит на элитрах
        if (!client.player.isFallFlying()) {
            client.player.sendMessage(Text.literal("Вы не летите на элитрах!"), true);
            return;
        }

        // Ищем фейерверк в хотбаре
        int fireworkSlot = findFireworkInHotbar(client);
        if (fireworkSlot == -1) {
            client.player.sendMessage(Text.literal("Фейерверки не найдены в хотбаре!"), true);
            return;
        }

        // Используем фейерверк
        useFirework(client, fireworkSlot);
    }

    private int findFireworkInHotbar(net.minecraft.client.MinecraftClient client) {
        if (client.player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(Items.FIREWORK_ROCKET)) {
                return i;
            }
        }
        return -1;
    }

    private void useFirework(net.minecraft.client.MinecraftClient client, int slot) {
        if (client.player == null || client.interactionManager == null) return;

        // Сохраняем текущий слот
        int previousSlot = client.player.getInventory().selectedSlot;

        try {
            // Переключаемся на слот с фейерверком
            client.player.getInventory().selectedSlot = slot;

            // Используем фейерверк
            client.interactionManager.interactItem(client.player, net.minecraft.util.Hand.MAIN_HAND);

        } finally {
            // Возвращаемся к предыдущему слоту
            client.player.getInventory().selectedSlot = previousSlot;
        }
    }
}
