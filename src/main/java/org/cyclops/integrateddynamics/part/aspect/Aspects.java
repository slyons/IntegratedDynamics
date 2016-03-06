package org.cyclops.integrateddynamics.part.aspect;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.api.capability.work.IWorker;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Collection of all aspects.
 * @author rubensworks
 */
public class Aspects {

    public static final IAspectRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IAspectRegistry.class);

    public static void load() {}

    public static final class Read {

        public static final class Block {
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_BLOCK =
                    AspectReadBuilders.Block.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<DimPos, Boolean>() {
                        @Override
                        public Boolean getOutput(DimPos dimPos) {
                            net.minecraft.block.Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
                            return block != Blocks.air;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "block").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_DIMENSION =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Integer>() {
                        @Override
                        public Integer getOutput(net.minecraft.world.World world) {
                            return world.provider.getDimensionId();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "dimension").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSX =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(new IAspectValuePropagator<BlockPos, Integer>() {
                        @Override
                        public Integer getOutput(BlockPos pos) {
                            return pos.getX();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "posx").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSY =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(new IAspectValuePropagator<BlockPos, Integer>() {
                        @Override
                        public Integer getOutput(BlockPos pos) {
                            return pos.getY();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "posy").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSZ =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(new IAspectValuePropagator<BlockPos, Integer>() {
                        @Override
                        public Integer getOutput(BlockPos pos) {
                            return pos.getZ();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "posz").buildRead();
            public static final IAspectRead<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock> BLOCK =
                    AspectReadBuilders.Block.BUILDER_BLOCK.handle(new IAspectValuePropagator<DimPos, IBlockState>() {
                        @Override
                        public IBlockState getOutput(DimPos dimPos) {
                            return dimPos.getWorld().getBlockState(dimPos.getBlockPos());
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BLOCK).buildRead();
        }

        public static final class Entity {
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ITEMFRAMEROTATION =
                    AspectReadBuilders.Entity.BUILDER_INTEGER_ALL
                            .handle(AspectReadBuilders.World.PROP_GET_ITEMFRAME)
                            .handle(new IAspectValuePropagator<EntityItemFrame, Integer>() {
                                @Override
                                public Integer getOutput(EntityItemFrame itemFrame) {
                                    return itemFrame!= null ? itemFrame.getRotation() : 0;
                                }
                            }).handle(AspectReadBuilders.PROP_GET_INTEGER, "itemframerotation").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ENTITIES =
                    AspectReadBuilders.Entity.BUILDER_LIST.handle(new IAspectValuePropagator<DimPos, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(DimPos dimPos) {
                            List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                                    new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), EntitySelectors.selectAnything);
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(entities, new Function<net.minecraft.entity.Entity, ValueObjectTypeEntity.ValueEntity>() {
                                @Nullable
                                @Override
                                public ValueObjectTypeEntity.ValueEntity apply(net.minecraft.entity.Entity input) {
                                    return ValueObjectTypeEntity.ValueEntity.of(input);
                                }
                            }));
                        }
                    }).appendKind("entities").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.Entity.BUILDER_LIST.handle(new IAspectValuePropagator<DimPos, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(DimPos dimPos) {
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(dimPos.getWorld().playerEntities, new Function<EntityPlayer, ValueObjectTypeEntity.ValueEntity>() {
                                @Nullable
                                @Override
                                public ValueObjectTypeEntity.ValueEntity apply(EntityPlayer input) {
                                    return ValueObjectTypeEntity.ValueEntity.of(input);
                                }
                            }));
                        }
                    }).appendKind("players").buildRead();

            public static final IAspectRead<ValueObjectTypeEntity.ValueEntity, ValueObjectTypeEntity> ENTITY =
                    AspectReadBuilders.Entity.BUILDER_ENTITY.withProperties(AspectReadBuilders.LIST_PROPERTIES).handle(new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueObjectTypeEntity.ValueEntity>() {
                        @Override
                        public ValueObjectTypeEntity.ValueEntity getOutput(Pair<PartTarget, IAspectProperties> input) {
                            int i = input.getRight().getValue(AspectReadBuilders.PROPERTY_LISTINDEX).getRawValue();
                            DimPos dimPos = input.getLeft().getTarget().getPos();
                            List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                                    new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), EntitySelectors.selectAnything);
                            return ValueObjectTypeEntity.ValueEntity.of(i < entities.size() ? entities.get(i) : null);
                        }
                    }).buildRead();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> ITEMSTACK_ITEMFRAMECONTENTS =
                    AspectReadBuilders.Entity.BUILDER_ITEMSTACK
                            .handle(AspectReadBuilders.World.PROP_GET_ITEMFRAME)
                            .handle(new IAspectValuePropagator<EntityItemFrame, ItemStack>() {
                                @Override
                                public ItemStack getOutput(EntityItemFrame itemFrame) {
                                    return itemFrame != null ? itemFrame.getDisplayedItem() : null;
                                }
                            }).handle(AspectReadBuilders.PROP_GET_ITEMSTACK, "itemframecontents").buildRead();
        }

        public static final class ExtraDimensional {

            private static final Random RANDOM = new Random();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_RANDOM =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(new IAspectValuePropagator<MinecraftServer, Integer>() {
                        @Override
                        public Integer getOutput(MinecraftServer minecraft) {
                            return RANDOM.nextInt();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "random").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_PLAYERCOUNT =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(new IAspectValuePropagator<MinecraftServer, Integer>() {
                        @Override
                        public Integer getOutput(MinecraftServer minecraft) {
                            return minecraft.getCurrentPlayerCount();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "playercount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TICKTIME =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(new IAspectValuePropagator<MinecraftServer, Integer>() {
                        @Override
                        public Integer getOutput(MinecraftServer minecraft) {
                            return (int) DoubleMath.mean(minecraft.tickTimeArray);
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "ticktime").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.ExtraDimensional.BUILDER_LIST.handle(new IAspectValuePropagator<MinecraftServer, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(MinecraftServer minecraft) {
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(minecraft.getConfigurationManager().playerEntityList, new Function<EntityPlayerMP, ValueObjectTypeEntity.ValueEntity>() {
                                @Nullable
                                @Override
                                public ValueObjectTypeEntity.ValueEntity apply(EntityPlayerMP input) {
                                    return ValueObjectTypeEntity.ValueEntity.of(input);
                                }
                            }));
                        }
                    }).appendKind("players").buildRead();

        }

        public static final class Fluid {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_FULL =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<FluidTankInfo[], Boolean>() {
                        @Override
                        public Boolean getOutput(FluidTankInfo[] tankInfo) {
                            boolean allFull = true;
                            for(FluidTankInfo tank : tankInfo) {
                                if(tank.fluid == null && tank.capacity > 0 || (tank.fluid != null && tank.fluid.amount < tank.capacity)) {
                                    allFull = false;
                                }
                            }
                            return allFull;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "full").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_EMPTY =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<FluidTankInfo[], Boolean>() {
                        @Override
                        public Boolean getOutput(FluidTankInfo[] tankInfo) {
                            for(FluidTankInfo tank : tankInfo) {
                                if(tank.fluid != null && tank.capacity > 0 || (tank.fluid != null && tank.fluid.amount < tank.capacity)) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "empty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONEMPTY =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<FluidTankInfo[], Boolean>() {
                        @Override
                        public Boolean getOutput(FluidTankInfo[] tankInfo) {
                            boolean hasFluid = false;
                            for(FluidTankInfo tank : tankInfo) {
                                if(tank.fluid != null && tank.fluid.amount > 0) {
                                    hasFluid = true;
                                }
                            }
                            return hasFluid;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<FluidTankInfo[], Boolean>() {
                        @Override
                        public Boolean getOutput(FluidTankInfo[] tankInfo) {
                            return tankInfo.length > 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_AMOUNT =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER_ACTIVATABLE.handle(AspectReadBuilders.Fluid.PROP_GET_FLUIDSTACK).handle(new IAspectValuePropagator<FluidStack, Integer>() {
                        @Override
                        public Integer getOutput(FluidStack fluidStack) {
                            return fluidStack != null ? fluidStack.amount : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "amount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_AMOUNTTOTAL =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(new IAspectValuePropagator<FluidTankInfo[], Integer>() {
                        @Override
                        public Integer getOutput(FluidTankInfo[] tankInfo) {
                            int amount = 0;
                            for(FluidTankInfo tank : tankInfo) {
                                if(tank.fluid != null) {
                                    amount += tank.fluid.amount;
                                }
                            }
                            return amount;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "totalamount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CAPACITY =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER_ACTIVATABLE.handle(new IAspectValuePropagator<FluidTankInfo, Integer>() {
                        @Override
                        public Integer getOutput(FluidTankInfo tankInfo) {
                            return tankInfo != null ? tankInfo.capacity : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "capacity").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CAPACITYTOTAL =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(new IAspectValuePropagator<FluidTankInfo[], Integer>() {
                        @Override
                        public Integer getOutput(FluidTankInfo[] tankInfo) {
                            int capacity = 0;
                            for(FluidTankInfo tank : tankInfo) {
                                capacity += tank.capacity;
                            }
                            return capacity;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "totalamount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TANKS =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(new IAspectValuePropagator<FluidTankInfo[], Integer>() {
                        @Override
                        public Integer getOutput(FluidTankInfo[] tankInfo) {
                            return tankInfo.length;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "tanks").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_FILLRATIO =
                    AspectReadBuilders.Fluid.BUILDER_DOUBLE_ACTIVATABLE.handle(new IAspectValuePropagator<FluidTankInfo, Double>() {
                        @Override
                        public Double getOutput(FluidTankInfo tankInfo) {
                            if(tankInfo == null) {
                                return 0D;
                            }
                            double amount = tankInfo.fluid == null ? 0D : tankInfo.fluid.amount;
                            return amount / (double) tankInfo.capacity;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_TANKFLUIDS =
                    AspectReadBuilders.BUILDER_LIST.appendKind("fluid").handle(AspectReadBuilders.Fluid.PROP_GET_LIST_FLUIDSTACKS, "fluidstacks").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_TANKCAPACITIES =
                    AspectReadBuilders.BUILDER_LIST.appendKind("fluid").handle(AspectReadBuilders.Fluid.PROP_GET_LIST_CAPACITIES, "capacities").buildRead();

            public static final IAspectRead<ValueObjectTypeFluidStack.ValueFluidStack, ValueObjectTypeFluidStack> FLUIDSTACK =
                    AspectReadBuilders.BUILDER_OBJECT_FLUIDSTACK
                            .handle(AspectReadBuilders.Fluid.PROP_GET_ACTIVATABLE, "fluid").withProperties(AspectReadBuilders.Fluid.PROPERTIES)
                            .handle(AspectReadBuilders.Fluid.PROP_GET_FLUIDSTACK).handle(AspectReadBuilders.PROP_GET_FLUIDSTACK).buildRead();

        }

        public static final class Inventory {
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_FULL =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IItemHandler, Boolean>() {
                        @Override
                        public Boolean getOutput(IItemHandler inventory) {
                            if(inventory != null) {
                                for (int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if (itemStack == null) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "full").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_EMPTY =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IItemHandler, Boolean>() {
                        @Override
                        public Boolean getOutput(IItemHandler inventory) {
                            if(inventory != null) {
                                for(int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if(itemStack != null) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "empty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONEMPTY =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IItemHandler, Boolean>() {
                        @Override
                        public Boolean getOutput(IItemHandler inventory) {
                            if(inventory != null) {
                                for(int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if(itemStack != null) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IItemHandler, Boolean>() {
                        @Override
                        public Boolean getOutput(IItemHandler inventory) {
                            return inventory != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_COUNT =
                    AspectReadBuilders.Inventory.BUILDER_INTEGER.handle(new IAspectValuePropagator<IItemHandler, Integer>() {
                        @Override
                        public Integer getOutput(IItemHandler inventory) {
                            int count = 0;
                            if(inventory != null) {
                                for (int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if (itemStack != null) {
                                        count += itemStack.stackSize;
                                    }
                                }
                            }
                            return count;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "count").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ITEMSTACKS =
                    AspectReadBuilders.BUILDER_LIST.appendKind("inventory").handle(AspectReadBuilders.Inventory.PROP_GET_LIST, "itemstacks").buildRead();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> OBJECT_ITEM_STACK_SLOT =
                    AspectReadBuilders.Inventory.BUILDER_ITEMSTACK.handle(AspectReadBuilders.PROP_GET_ITEMSTACK).buildRead();

        }

        public static final class Machine {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISWORKER =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(new IAspectValuePropagator<IWorker, Boolean>() {
                        @Override
                        public Boolean getOutput(IWorker worker) {
                            return worker != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isworker").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HASWORK =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(new IAspectValuePropagator<IWorker, Boolean>() {
                        @Override
                        public Boolean getOutput(IWorker worker) {
                            return worker != null && worker.hasWork();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "haswork").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANWORK =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(new IAspectValuePropagator<IWorker, Boolean>() {
                        @Override
                        public Boolean getOutput(IWorker worker) {
                            return worker != null && worker.canWork();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "canwork").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISWORKING =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(new IAspectValuePropagator<IWorker, Boolean>() {
                        @Override
                        public Boolean getOutput(IWorker worker) {
                            return worker != null && worker.canWork() && worker.hasWork();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isworking").buildRead();

        }

        public static final class Network {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Network.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<INetwork, Boolean>() {
                        @Override
                        public Boolean getOutput(INetwork network) {
                            return network != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ELEMENT_COUNT =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(new IAspectValuePropagator<INetwork, Integer>() {
                        @Override
                        public Integer getOutput(INetwork network) {
                            return network != null ? network.getElements().size() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "elementcount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_BATTERY_COUNT =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(new IAspectValuePropagator<INetwork, Integer>() {
                        @Override
                        public Integer getOutput(INetwork network) {
                            return network != null ? (network instanceof IEnergyNetwork ? ((IEnergyNetwork) network).getEnergyBatteries().size() : 0) : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("batterycount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_STORED =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(new IAspectValuePropagator<INetwork, Integer>() {
                        @Override
                        public Integer getOutput(INetwork network) {
                            return network != null ? (network instanceof IEnergyNetwork ? ((IEnergyNetwork) network).getStoredEnergy() : 0) : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("stored").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_MAX =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(new IAspectValuePropagator<INetwork, Integer>() {
                        @Override
                        public Integer getOutput(INetwork network) {
                            return network != null ? (network instanceof IEnergyNetwork ? ((IEnergyNetwork) network).getMaxStoredEnergy() : 0) : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("max").buildRead();

        }

        public static final class Redstone {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_LOW =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Integer, Boolean>() {
                        @Override
                        public Boolean getOutput(Integer input) {
                            return input == 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "low").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONLOW =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Integer, Boolean>() {
                        @Override
                        public Boolean getOutput(Integer input) {
                            return input > 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonlow").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HIGH =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Integer, Boolean>() {
                        @Override
                        public Boolean getOutput(Integer input) {
                            return input == 15;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "high").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_VALUE =
                    AspectReadBuilders.Redstone.BUILDER_INTEGER.handle(AspectReadBuilders.PROP_GET_INTEGER, "value").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_COMPARATOR =
                    AspectReadBuilders.Redstone.BUILDER_INTEGER_COMPARATOR.handle(AspectReadBuilders.PROP_GET_INTEGER, "comparator").buildRead();

        }

        public static final class World {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_CLEAR =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return !world.isRaining();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("clear").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_RAINING =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return world.isRaining();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("raining").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_THUNDER =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return world.isThundering();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("thunder").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISDAY =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return MinecraftHelpers.isDay(world);
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isday").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISNIGHT =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return !MinecraftHelpers.isDay(world);
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isnight").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_RAINCOUNTDOWN =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Integer>() {
                        @Override
                        public Integer getOutput(net.minecraft.world.World world) {
                            return world.getWorldInfo().getRainTime();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "raincountdown").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TICKTIME =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Integer>() {
                        @Override
                        public Integer getOutput(net.minecraft.world.World world) {
                            return (int) DoubleMath.mean(MinecraftServer.getServer().worldTickTimes.get(world.provider.getDimensionId()));
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "ticktime").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_DAYTIME =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Integer>() {
                        @Override
                        public Integer getOutput(net.minecraft.world.World world) {
                            return (int) world.getWorldTime() % MinecraftHelpers.MINECRAFT_DAY;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "daytime").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_LIGHTLEVEL =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(new IAspectValuePropagator<DimPos, Integer>() {
                        @Override
                        public Integer getOutput(DimPos dimPos) {
                            return dimPos.getWorld().getLight(dimPos.getBlockPos());
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "lightlevel").buildRead();

            public static final IAspectRead<ValueTypeLong.ValueLong, ValueTypeLong> LONG_TIME =
                    AspectReadBuilders.World.BUILDER_LONG.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Long>() {
                        @Override
                        public Long getOutput(net.minecraft.world.World world) {
                            return world.getWorldTime();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_LONG, "time").buildRead();
            public static final IAspectRead<ValueTypeLong.ValueLong, ValueTypeLong> LONG_TOTALTIME =
                    AspectReadBuilders.World.BUILDER_LONG.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Long>() {
                        @Override
                        public Long getOutput(net.minecraft.world.World world) {
                            return world.getTotalWorldTime();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_LONG, "totaltime").buildRead();

            public static final IAspectRead<ValueTypeString.ValueString, ValueTypeString> STRING_NAME =
                    AspectReadBuilders.World.BUILDER_STRING.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, String>() {
                        @Override
                        public String getOutput(net.minecraft.world.World world) {
                            return world.getWorldInfo().getWorldName();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_STRING, "worldname").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.World.BUILDER_LIST.handle(new IAspectValuePropagator<DimPos, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(DimPos dimPos) {
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(dimPos.getWorld().playerEntities, new Function<EntityPlayer, ValueObjectTypeEntity.ValueEntity>() {
                                @Nullable
                                @Override
                                public ValueObjectTypeEntity.ValueEntity apply(EntityPlayer input) {
                                    return ValueObjectTypeEntity.ValueEntity.of(input);
                                }
                            }));
                        }
                    }).appendKind("players").buildRead();

        }

    }

    public static final class Write {

        public static final class Audio {

            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_PIANO_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.PIANO), "piano")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_BASSDRUM_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.BASSDRUM), "bassdrum")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_SNARE_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.SNARE), "snare")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CLICKS_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.CLICKS), "clicks")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> NOTE_INTEGER_BASSGUITAR =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.BASSGUITAR), "bassguitar")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();

        }

        public static final class Redstone {

            public static final IAspectWrite<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN =
                    AspectWriteBuilders.Redstone.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Boolean>, Triple<PartTarget, IAspectProperties, Integer>>() {
                        @Override
                        public Triple<PartTarget, IAspectProperties, Integer> getOutput(Triple<PartTarget, IAspectProperties, Boolean> input) throws EvaluationException {
                            return Triple.of(input.getLeft(), input.getMiddle(), input.getRight() ? 15 : 0);
                        }
                    }).handle(AspectWriteBuilders.Redstone.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER =
                    AspectWriteBuilders.Redstone.BUILDER_INTEGER.handle(AspectWriteBuilders.Redstone.PROP_SET).buildWrite();

        }

    }

}
