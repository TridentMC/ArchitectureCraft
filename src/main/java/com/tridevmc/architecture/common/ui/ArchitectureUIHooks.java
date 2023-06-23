package com.tridevmc.architecture.common.ui;

import com.tridevmc.architecture.common.ArchitectureMod;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Optional;

/**
 * Lifted from DV - Should probably move this into compound as it makes things nice to work with...
 */
public class ArchitectureUIHooks {

    private static Optional<IElementProvider<AbstractContainerMenu>> lastProvider = Optional.empty();

    public static <C extends AbstractContainerMenu> MenuType<C> register(RegisterEvent.RegisterHelper<MenuType<?>> registry) {
        MenuType<C> containerType = IForgeMenuType.create(getFactory());
        registry.register(new ResourceLocation(ArchitectureMod.MOD_ID, "containers"), containerType);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MenuScreens.register(containerType, getScreenFactory()));
        return containerType;
    }

    private static <C extends AbstractContainerMenu> IContainerFactory<C> getFactory() {
        return (windowId, inv, data) -> {
            UIType type = UIType.byId(data.readByte());
            CreateMenuContext context = new CreateMenuContext(windowId, inv.player, inv);
            var world = inv.player.level();

            switch (type) {
                case TILE:
                    var pos = data.readBlockPos();
                    var state = world.getBlockState(pos);
                    var tile = world.getBlockEntity(pos);
                    context.setPos(pos).setBlockState(state).setBlockEntity(tile);
                    if (tile instanceof IElementProvider) {
                        lastProvider = Optional.of((IElementProvider) tile);
                        return (C) ((IElementProvider) tile).createMenu(context);
                    } else if (state.getBlock() instanceof IElementProvider) {
                        lastProvider = Optional.of((IElementProvider) state.getBlock());
                        return (C) ((IElementProvider) state.getBlock()).createMenu(context);
                    }
                case ENTITY:
                    int entityId = data.readVarInt();
                    var entity = world.getEntity(entityId);
                    context.setEntity(entity);
                    if (entity instanceof IElementProvider) {
                        lastProvider = Optional.of((IElementProvider) entity);
                        return (C) ((IElementProvider) entity).createMenu(context);
                    }
                default:
                    lastProvider = Optional.empty();
                    return null;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    private static MenuScreens.ScreenConstructor getScreenFactory() {
        return (container, inv, name) -> lastProvider.map(eP -> eP.createScreen(container, inv.player)).orElse(null);
    }

    public static void openGui(Player player, IElementProvider<AbstractContainerMenu> provider) {
        if (player instanceof ServerPlayer) {
            if (provider instanceof BlockEntity) {
                openGui((ServerPlayer) player, provider, ((BlockEntity) provider).getBlockPos());
            } else if (provider instanceof Entity) {
                openGui((ServerPlayer) player, provider, ((Entity) provider).getId());
            }
        } else {
            throw new ClassCastException(String.format("Unable to cast type %s to ServerPlayerEntity", player.getClass().getName()));
        }
    }

    public static void openGui(ServerPlayer player, IElementProvider<? extends AbstractContainerMenu> provider, BlockPos pos) {
        NetworkHooks.openScreen(player, provider, packetBuffer -> {
            packetBuffer.writeByte(UIType.TILE.id);
            packetBuffer.writeBlockPos(pos);
        });
    }

    public static void openGui(ServerPlayer player, IElementProvider<? extends AbstractContainerMenu> provider, int entity) {
        NetworkHooks.openScreen(player, provider, packetBuffer -> {
            packetBuffer.writeByte(UIType.ENTITY.id);
            packetBuffer.writeVarInt(entity);
        });
    }

    private enum UIType {
        TILE(0), ENTITY(1), OTHER(2);

        private static final UIType[] TYPES;

        static {
            TYPES = new UIType[]{TILE, ENTITY, OTHER};
        }

        private final int id;

        UIType(int id) {
            this.id = id;
        }

        public static UIType byId(int id) {
            return TYPES[id];
        }

        public int getId() {
            return this.id;
        }
    }

}
