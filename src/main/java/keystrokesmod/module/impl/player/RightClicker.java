package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.other.RecordClick;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Method;
import java.util.Random;

public class RightClicker extends Module {
    public ModeSetting mode;
    public SliderSetting minCPS;
    public SliderSetting maxCPS;
    public SliderSetting jitter;
    public static ButtonSetting rightClick;
    public ButtonSetting blocksOnly;
    private final ModeSetting clickSound;
    private Random rand = null;
    private long nextReleaseClickTime;
    private long nextClickTime;
    private Method gs;

    public RightClicker() {
        super("RightClicker", Module.category.player, 0);
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"CPS", "Record"}, 0));
        final ModeOnly mode0 = new ModeOnly(mode, 0);
        this.registerSetting(minCPS = new SliderSetting("Min CPS", 9.0, 1.0, 20.0, 0.5, mode0));
        this.registerSetting(maxCPS = new SliderSetting("Max CPS", 12.0, 1.0, 20.0, 0.5, mode0));
        this.registerSetting(jitter = new SliderSetting("Jitter", 0.0, 0.0, 3.0, 0.1));
        this.registerSetting(blocksOnly = new ButtonSetting("Blocks only", true));
        this.registerSetting(clickSound = new ModeSetting("Click sound", new String[]{"None", "Standard", "Double", "Alan"}, 0));

        
        try {
            this.gs = GuiScreen.class.getDeclaredMethod("func_73864_a", Integer.TYPE, Integer.TYPE, Integer.TYPE);
        } catch (Exception var4) {
            try {
                this.gs = GuiScreen.class.getDeclaredMethod("mouseClicked", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            } catch (Exception ignored) {
            }
        }

        if (this.gs != null) {
            this.gs.setAccessible(true);
        }
    }

    public void onEnable() {
        this.rand = new Random();
    }

    public void onDisable() {
        this.nextReleaseClickTime = 0L;
        this.nextClickTime = 0L;
    }

    public void guiUpdate() {
        Utils.correctValue(minCPS, maxCPS);
    }

    @SubscribeEvent
    public void onRenderTick(@NotNull RenderTickEvent ev) {
        if (ev.phase != Phase.END && Utils.nullCheck() && !mc.thePlayer.isEating() && mc.objectMouseOver != null) {
            if (mc.currentScreen == null && mc.inGameHasFocus) {
                if (Mouse.isButtonDown(1)) {
                    if (blocksOnly.isToggled() && (mc.thePlayer.getCurrentEquippedItem() == null || !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock))) {
                        return;
                    }
                    this.dc(mc.gameSettings.keyBindUseItem.getKeyCode(), 1);
                } else {
                    this.nextReleaseClickTime = 0L;
                    this.nextClickTime = 0L;
                }
            }
        }
    }

    public void dc(int key, int mouse) {
        if (this.nextClickTime > 0L && this.nextReleaseClickTime > 0L) {
            if (System.currentTimeMillis() > this.nextClickTime && KillAura.target == null && !ModuleManager.killAura.swing) {
                if (jitter.getInput() > 0.0D) {
            double a = jitter.getInput() * 0.45D;
            EntityPlayerSP var10000;
            if (this.rand.nextBoolean()) {
                var10000 = mc.thePlayer;
                var10000.rotationYaw = (float) ((double) var10000.rotationYaw + (double) this.rand.nextFloat() * a);
            } else {
                var10000 = mc.thePlayer;
                var10000.rotationYaw = (float) ((double) var10000.rotationYaw - (double) this.rand.nextFloat() * a);
            }

            if (this.rand.nextBoolean()) {
                var10000 = mc.thePlayer;
                var10000.rotationPitch = (float) ((double) var10000.rotationPitch + (double) this.rand.nextFloat() * a * 0.45D);
            } else {
                var10000 = mc.thePlayer;
                var10000.rotationPitch = (float) ((double) var10000.rotationPitch - (double) this.rand.nextFloat() * a * 0.45D);
            }
        }
                KeyBinding.setKeyBindState(key, true);
                RecordClick.click();
                KeyBinding.onTick(key);
                Reflection.setButton(mouse, true);
                if (clickSound.getInput() != 0) {
                    mc.thePlayer.playSound(
                            "keystrokesmod:click." + clickSound.getOptions()[(int) clickSound.getInput()].toLowerCase()
                            , 1, 1
                    );
                }
                this.gd();

            } else if (System.currentTimeMillis() > this.nextReleaseClickTime) {

                KeyBinding.setKeyBindState(key, false);
                Reflection.setButton(mouse, false);
            }
        } else {
            this.gd();
        }
    }

    public void gd() {
        switch ((int) mode.getInput()) {
            case 0:
                double c = Utils.getRandomValue(minCPS, maxCPS, this.rand) + 0.4D * this.rand.nextDouble();
                long d = (int) Math.round(1000.0D / c);
                this.nextClickTime = System.currentTimeMillis() + d;
                this.nextReleaseClickTime = System.currentTimeMillis() + d / 2L - (long) this.rand.nextInt(10);
                break;
            case 1:
                this.nextClickTime = RecordClick.getNextClickTime();
                this.nextReleaseClickTime = this.nextClickTime + 1;
                break;
        }
    }
}