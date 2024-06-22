/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast.injection.processors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class InjectOnAllReturnsProcessor {

    public static void process(final ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.visibleAnnotations == null) continue;
            for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
                if (annotationNode.desc.equals(Type.getDescriptor(InjectOnAllReturns.class))) {
                    if (!methodNode.desc.equals("(" + Type.getDescriptor(CallbackInfo.class) + ")V")) {
                        throw new RuntimeException("Method annotated with @InjectOnAllReturns must have signature void(CallbackInfo)");
                    }
                    final boolean isStatic = (methodNode.access & Opcodes.ACC_STATIC) != 0;

                    for (MethodNode methodNode2 : classNode.methods) {
                        boolean shouldInject = false;
                        for (AbstractInsnNode insnNode : methodNode2.instructions.toArray()) {
                            if (insnNode instanceof MethodInsnNode methodInsnNode) {
                                if (methodInsnNode.owner.equals(classNode.name) && methodInsnNode.name.equals(methodNode.name) && methodInsnNode.desc.equals(methodNode.desc)) {
                                    if (!isStatic) {
                                        methodNode2.instructions.set(insnNode, new InsnNode(Opcodes.POP2));
                                    } else {
                                        methodNode2.instructions.set(insnNode, new InsnNode(Opcodes.POP));
                                    }
                                    shouldInject = true;
                                }
                            }
                        }
                        if (shouldInject) {
                            for (AbstractInsnNode insnNode : methodNode2.instructions.toArray()) {
                                if (insnNode.getOpcode() >= Opcodes.IRETURN && insnNode.getOpcode() <= Opcodes.RETURN) {
                                    final InsnList inject = new InsnList();
                                    if (!isStatic) {
                                        inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    }
                                    inject.add(new InsnNode(Opcodes.ACONST_NULL));
                                    if (!isStatic) {
                                        inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classNode.name, methodNode.name, methodNode.desc, (classNode.access & Opcodes.ACC_INTERFACE) != 0));
                                    } else {
                                        inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, methodNode.name, methodNode.desc, (classNode.access & Opcodes.ACC_INTERFACE) != 0));
                                    }
                                    methodNode2.instructions.insertBefore(insnNode, inject);
                                }
                            }
                        }
                    }
                    methodNode.visibleAnnotations.remove(annotationNode);
                    break;
                }
            }
        }
    }

}
