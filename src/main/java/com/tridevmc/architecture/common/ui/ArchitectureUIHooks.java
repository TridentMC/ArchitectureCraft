package com.tridevmc.architecture.common.ui;

import com.tridevmc.architecture.common.ArchitectureMod;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Lifted from DV - Should probably move this into compound as it makes things nice to work with...
 */
public class ArchitectureUIHooks {

    private static Optional<IElementProvider<AbstractContainerMenu>> lastProvider = Optional.empty();
    private static Consumer<RegisterMenuScreensEvent> registerMenuScreensEvent;

    public static <C extends AbstractContainerMenu> MenuType<C> register(RegisterEvent.RegisterHelper<MenuType<?>> registry) {
        MenuType<C> containerType = IMenuTypeExtension.create(getFactory());
        registry.register(ResourceLocation.fromNamespaceAndPath(ArchitectureMod.MOD_ID, "containers"), containerType);
        // This is honestly the worst solution to this problem, but it works for now.
        registerMenuScreensEvent = event -> event.register(containerType, ArchitectureUIHooks.getScreenFactory());
        return containerType;
    }

    public static void register(RegisterMenuScreensEvent event) {
        registerMenuScreensEvent.accept(event);
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
            if (provider instanceof BlockEntity be) {
                openGui((ServerPlayer) player, provider, be);
            } else if (provider instanceof Entity e) {
                openGui((ServerPlayer) player, provider, e);
            }
        } else {
            throw new ClassCastException(String.format("Unable to cast type %s to ServerPlayerEntity", player.getClass().getName()));
        }
    }

    public static void openGui(ServerPlayer player, IElementProvider<? extends AbstractContainerMenu> provider, BlockEntity blockEntity) {
        player.openMenu(new MenuProvider() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory inventory, @NotNull Player player) {
                return provider.createMenu(new CreateMenuContext(pContainerId, player, inventory).setBlockState(blockEntity.getBlockState()).setBlockEntity(blockEntity).setPos(blockEntity.getBlockPos()));
            }

            @Override
            public @NotNull Component getDisplayName() {
                return provider.getDisplayName();
            }
        });
    }

    public static void openGui(ServerPlayer player, IElementProvider<? extends AbstractContainerMenu> provider, BlockPos pos) {
        player.openMenu(new MenuProvider() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory inventory, @NotNull Player player) {
                return provider.createMenu(new CreateMenuContext(pContainerId, player, inventory).setPos(pos));
            }

            @Override
            public @NotNull Component getDisplayName() {
                return provider.getDisplayName();
            }
        });
    }

    public static void openGui(ServerPlayer player, IElementProvider<? extends AbstractContainerMenu> provider, Entity entity) {
        player.openMenu(new MenuProvider() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory inventory, @NotNull Player player) {
                return provider.createMenu(new CreateMenuContext(pContainerId, player, inventory).setEntity(entity));
            }

            @Override
            public @NotNull Component getDisplayName() {
                return provider.getDisplayName();
            }
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
