/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pranav.ide.dx.cf.direct;

import com.pranav.ide.dx.cf.attrib.AttAnnotationDefault;
import com.pranav.ide.dx.cf.attrib.AttBootstrapMethods;
import com.pranav.ide.dx.cf.attrib.AttCode;
import com.pranav.ide.dx.cf.attrib.AttConstantValue;
import com.pranav.ide.dx.cf.attrib.AttDeprecated;
import com.pranav.ide.dx.cf.attrib.AttEnclosingMethod;
import com.pranav.ide.dx.cf.attrib.AttExceptions;
import com.pranav.ide.dx.cf.attrib.AttInnerClasses;
import com.pranav.ide.dx.cf.attrib.AttLineNumberTable;
import com.pranav.ide.dx.cf.attrib.AttLocalVariableTable;
import com.pranav.ide.dx.cf.attrib.AttLocalVariableTypeTable;
import com.pranav.ide.dx.cf.attrib.AttRuntimeInvisibleAnnotations;
import com.pranav.ide.dx.cf.attrib.AttRuntimeInvisibleParameterAnnotations;
import com.pranav.ide.dx.cf.attrib.AttRuntimeVisibleAnnotations;
import com.pranav.ide.dx.cf.attrib.AttRuntimeVisibleParameterAnnotations;
import com.pranav.ide.dx.cf.attrib.AttSignature;
import com.pranav.ide.dx.cf.attrib.AttSourceDebugExtension;
import com.pranav.ide.dx.cf.attrib.AttSourceFile;
import com.pranav.ide.dx.cf.attrib.AttSynthetic;
import com.pranav.ide.dx.cf.attrib.InnerClassList;
import com.pranav.ide.dx.cf.code.BootstrapMethodArgumentsList;
import com.pranav.ide.dx.cf.code.BootstrapMethodsList;
import com.pranav.ide.dx.cf.code.ByteCatchList;
import com.pranav.ide.dx.cf.code.BytecodeArray;
import com.pranav.ide.dx.cf.code.LineNumberList;
import com.pranav.ide.dx.cf.code.LocalVariableList;
import com.pranav.ide.dx.cf.iface.Attribute;
import com.pranav.ide.dx.cf.iface.ParseException;
import com.pranav.ide.dx.cf.iface.ParseObserver;
import com.pranav.ide.dx.cf.iface.StdAttributeList;
import com.pranav.ide.dx.rop.annotation.AnnotationVisibility;
import com.pranav.ide.dx.rop.annotation.Annotations;
import com.pranav.ide.dx.rop.annotation.AnnotationsList;
import com.pranav.ide.dx.rop.code.AccessFlags;
import com.pranav.ide.dx.rop.cst.Constant;
import com.pranav.ide.dx.rop.cst.ConstantPool;
import com.pranav.ide.dx.rop.cst.CstMethodHandle;
import com.pranav.ide.dx.rop.cst.CstNat;
import com.pranav.ide.dx.rop.cst.CstString;
import com.pranav.ide.dx.rop.cst.CstType;
import com.pranav.ide.dx.rop.cst.TypedConstant;
import com.pranav.ide.dx.rop.type.TypeList;
import com.pranav.ide.dx.util.ByteArray;
import com.pranav.ide.dx.util.Hex;

import java.io.IOException;

/**
 * Standard subclass of {@link com.pranav.ide.dx.cf.direct.AttributeFactory}, which knows how to parse
 * all the standard attribute types.
 */
public class StdAttributeFactory
        extends AttributeFactory {
    /**
     * {@code non-null;} shared instance of this class
     */
    public static final StdAttributeFactory THE_ONE =
            new StdAttributeFactory();

    /**
     * Constructs an instance.
     */
    public StdAttributeFactory() {
        // This space intentionally left blank.
    }

    /**
     * Throws the right exception when a known attribute has a way too short
     * length.
     *
     * @return never
     * @throws com.pranav.ide.dx.cf.iface.ParseException always thrown
     */
    private static com.pranav.ide.dx.cf.iface.Attribute throwSeverelyTruncated() {
        throw new com.pranav.ide.dx.cf.iface.ParseException("severely truncated attribute");
    }

    /**
     * Throws the right exception when a known attribute has a too short
     * length.
     *
     * @return never
     * @throws com.pranav.ide.dx.cf.iface.ParseException always thrown
     */
    private static com.pranav.ide.dx.cf.iface.Attribute throwTruncated() {
        throw new com.pranav.ide.dx.cf.iface.ParseException("truncated attribute");
    }

    /**
     * Throws the right exception when an attribute has an unexpected length
     * (given its contents).
     *
     * @param expected expected length
     * @return never
     * @throws com.pranav.ide.dx.cf.iface.ParseException always thrown
     */
    private static Attribute throwBadLength(int expected) {
        throw new com.pranav.ide.dx.cf.iface.ParseException("bad attribute length; expected length " +
                com.pranav.ide.dx.util.Hex.u4(expected));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected com.pranav.ide.dx.cf.iface.Attribute parse0(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int context, String name,
                                                          int offset, int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        switch (context) {
            case CTX_CLASS: {
                if (name == com.pranav.ide.dx.cf.attrib.AttBootstrapMethods.ATTRIBUTE_NAME) {
                    return bootstrapMethods(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttDeprecated.ATTRIBUTE_NAME) {
                    return deprecated(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttEnclosingMethod.ATTRIBUTE_NAME) {
                    return enclosingMethod(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttInnerClasses.ATTRIBUTE_NAME) {
                    return innerClasses(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeInvisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeVisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttSynthetic.ATTRIBUTE_NAME) {
                    return synthetic(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttSignature.ATTRIBUTE_NAME) {
                    return signature(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttSourceDebugExtension.ATTRIBUTE_NAME) {
                    return sourceDebugExtension(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttSourceFile.ATTRIBUTE_NAME) {
                    return sourceFile(cf, offset, length, observer);
                }
                break;
            }
            case CTX_FIELD: {
                if (name == com.pranav.ide.dx.cf.attrib.AttConstantValue.ATTRIBUTE_NAME) {
                    return constantValue(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttDeprecated.ATTRIBUTE_NAME) {
                    return deprecated(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeInvisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeVisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttSignature.ATTRIBUTE_NAME) {
                    return signature(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttSynthetic.ATTRIBUTE_NAME) {
                    return synthetic(cf, offset, length, observer);
                }
                break;
            }
            case CTX_METHOD: {
                if (name == com.pranav.ide.dx.cf.attrib.AttAnnotationDefault.ATTRIBUTE_NAME) {
                    return annotationDefault(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttCode.ATTRIBUTE_NAME) {
                    return code(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttDeprecated.ATTRIBUTE_NAME) {
                    return deprecated(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttExceptions.ATTRIBUTE_NAME) {
                    return exceptions(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeInvisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeVisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttRuntimeInvisibleParameterAnnotations.
                        ATTRIBUTE_NAME) {
                    return runtimeInvisibleParameterAnnotations(
                            cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttRuntimeVisibleParameterAnnotations.
                        ATTRIBUTE_NAME) {
                    return runtimeVisibleParameterAnnotations(
                            cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttSignature.ATTRIBUTE_NAME) {
                    return signature(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttSynthetic.ATTRIBUTE_NAME) {
                    return synthetic(cf, offset, length, observer);
                }
                break;
            }
            case CTX_CODE: {
                if (name == com.pranav.ide.dx.cf.attrib.AttLineNumberTable.ATTRIBUTE_NAME) {
                    return lineNumberTable(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttLocalVariableTable.ATTRIBUTE_NAME) {
                    return localVariableTable(cf, offset, length, observer);
                }
                if (name == com.pranav.ide.dx.cf.attrib.AttLocalVariableTypeTable.ATTRIBUTE_NAME) {
                    return localVariableTypeTable(cf, offset, length,
                            observer);
                }
                break;
            }
        }

        return super.parse0(cf, context, name, offset, length, observer);
    }

    /**
     * Parses an {@code AnnotationDefault} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute annotationDefault(com.pranav.ide.dx.cf.direct.DirectClassFile cf,
                                                                   int offset, int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }

        com.pranav.ide.dx.cf.direct.AnnotationParser ap =
                new com.pranav.ide.dx.cf.direct.AnnotationParser(cf, offset, length, observer);
        com.pranav.ide.dx.rop.cst.Constant cst = ap.parseValueAttribute();

        return new AttAnnotationDefault(cst, length);
    }

    /**
     * Parses a {@code BootstrapMethods} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute bootstrapMethods(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                                  com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        int numMethods = bytes.getUnsignedShort(offset);
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "num_boostrap_methods: " + com.pranav.ide.dx.util.Hex.u2(numMethods));
        }

        offset += 2;
        length -= 2;

        com.pranav.ide.dx.cf.code.BootstrapMethodsList methods = parseBootstrapMethods(bytes, cf.getConstantPool(),
                cf.getThisClass(), numMethods,
                offset, length, observer);
        return new AttBootstrapMethods(methods);
    }

    /**
     * Parses a {@code Code} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute code(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                      com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 12) {
            return throwSeverelyTruncated();
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        com.pranav.ide.dx.rop.cst.ConstantPool pool = cf.getConstantPool();
        int maxStack = bytes.getUnsignedShort(offset); // u2 max_stack
        int maxLocals = bytes.getUnsignedShort(offset + 2); // u2 max_locals
        int codeLength = bytes.getInt(offset + 4); // u4 code_length
        int origOffset = offset;

        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "max_stack: " + com.pranav.ide.dx.util.Hex.u2(maxStack));
            observer.parsed(bytes, offset + 2, 2,
                    "max_locals: " + com.pranav.ide.dx.util.Hex.u2(maxLocals));
            observer.parsed(bytes, offset + 4, 4,
                    "code_length: " + com.pranav.ide.dx.util.Hex.u4(codeLength));
        }

        offset += 8;
        length -= 8;

        if (length < (codeLength + 4)) {
            return throwTruncated();
        }

        int codeOffset = offset;
        offset += codeLength;
        length -= codeLength;
        com.pranav.ide.dx.cf.code.BytecodeArray code =
                new BytecodeArray(bytes.slice(codeOffset, codeOffset + codeLength),
                        pool);
        if (observer != null) {
            code.forEach(new CodeObserver(code.getBytes(), observer));
        }

        // u2 exception_table_length
        int exceptionTableLength = bytes.getUnsignedShort(offset);
        com.pranav.ide.dx.cf.code.ByteCatchList catches = (exceptionTableLength == 0) ?
                com.pranav.ide.dx.cf.code.ByteCatchList.EMPTY :
                new ByteCatchList(exceptionTableLength);

        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "exception_table_length: " +
                            com.pranav.ide.dx.util.Hex.u2(exceptionTableLength));
        }

        offset += 2;
        length -= 2;

        if (length < (exceptionTableLength * 8 + 2)) {
            return throwTruncated();
        }

        for (int i = 0; i < exceptionTableLength; i++) {
            if (observer != null) {
                observer.changeIndent(1);
            }

            int startPc = bytes.getUnsignedShort(offset);
            int endPc = bytes.getUnsignedShort(offset + 2);
            int handlerPc = bytes.getUnsignedShort(offset + 4);
            int catchTypeIdx = bytes.getUnsignedShort(offset + 6);
            com.pranav.ide.dx.rop.cst.CstType catchType = (com.pranav.ide.dx.rop.cst.CstType) pool.get0Ok(catchTypeIdx);
            catches.set(i, startPc, endPc, handlerPc, catchType);
            if (observer != null) {
                observer.parsed(bytes, offset, 8,
                        com.pranav.ide.dx.util.Hex.u2(startPc) + ".." + com.pranav.ide.dx.util.Hex.u2(endPc) +
                                " -> " + com.pranav.ide.dx.util.Hex.u2(handlerPc) + " " +
                                ((catchType == null) ? "<any>" :
                                        catchType.toHuman()));
            }
            offset += 8;
            length -= 8;

            if (observer != null) {
                observer.changeIndent(-1);
            }
        }

        catches.setImmutable();

        com.pranav.ide.dx.cf.direct.AttributeListParser parser =
                new com.pranav.ide.dx.cf.direct.AttributeListParser(cf, CTX_CODE, offset, this);
        parser.setObserver(observer);

        StdAttributeList attributes = parser.getList();
        attributes.setImmutable();

        int attributeByteCount = parser.getEndOffset() - offset;
        if (attributeByteCount != length) {
            return throwBadLength(attributeByteCount + (offset - origOffset));
        }

        return new AttCode(maxStack, maxLocals, code, catches, attributes);
    }

    /**
     * Parses a {@code ConstantValue} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute constantValue(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                               com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length != 2) {
            return throwBadLength(2);
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        com.pranav.ide.dx.rop.cst.ConstantPool pool = cf.getConstantPool();
        int idx = bytes.getUnsignedShort(offset);
        com.pranav.ide.dx.rop.cst.TypedConstant cst = (TypedConstant) pool.get(idx);
        com.pranav.ide.dx.cf.iface.Attribute result = new AttConstantValue(cst);

        if (observer != null) {
            observer.parsed(bytes, offset, 2, "value: " + cst);
        }

        return result;
    }

    /**
     * Parses a {@code Deprecated} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute deprecated(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                            com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length != 0) {
            return throwBadLength(0);
        }

        return new AttDeprecated();
    }

    /**
     * Parses an {@code EnclosingMethod} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute enclosingMethod(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset,
                                                                 int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length != 4) {
            throwBadLength(4);
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        com.pranav.ide.dx.rop.cst.ConstantPool pool = cf.getConstantPool();

        int idx = bytes.getUnsignedShort(offset);
        com.pranav.ide.dx.rop.cst.CstType type = (com.pranav.ide.dx.rop.cst.CstType) pool.get(idx);

        idx = bytes.getUnsignedShort(offset + 2);
        com.pranav.ide.dx.rop.cst.CstNat method = (CstNat) pool.get0Ok(idx);

        com.pranav.ide.dx.cf.iface.Attribute result = new AttEnclosingMethod(type, method);

        if (observer != null) {
            observer.parsed(bytes, offset, 2, "class: " + type);
            observer.parsed(bytes, offset + 2, 2, "method: " +
                    com.pranav.ide.dx.cf.direct.DirectClassFile.stringOrNone(method));
        }

        return result;
    }

    /**
     * Parses an {@code Exceptions} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute exceptions(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                            com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        int count = bytes.getUnsignedShort(offset); // number_of_exceptions

        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "number_of_exceptions: " + com.pranav.ide.dx.util.Hex.u2(count));
        }

        offset += 2;
        length -= 2;

        if (length != (count * 2)) {
            throwBadLength((count * 2) + 2);
        }

        TypeList list = cf.makeTypeList(offset, count);
        return new AttExceptions(list);
    }

    /**
     * Parses an {@code InnerClasses} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute innerClasses(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                              com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        com.pranav.ide.dx.rop.cst.ConstantPool pool = cf.getConstantPool();
        int count = bytes.getUnsignedShort(offset); // number_of_classes

        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "number_of_classes: " + com.pranav.ide.dx.util.Hex.u2(count));
        }

        offset += 2;
        length -= 2;

        if (length != (count * 8)) {
            throwBadLength((count * 8) + 2);
        }

        com.pranav.ide.dx.cf.attrib.InnerClassList list = new InnerClassList(count);

        for (int i = 0; i < count; i++) {
            int innerClassIdx = bytes.getUnsignedShort(offset);
            int outerClassIdx = bytes.getUnsignedShort(offset + 2);
            int nameIdx = bytes.getUnsignedShort(offset + 4);
            int accessFlags = bytes.getUnsignedShort(offset + 6);
            com.pranav.ide.dx.rop.cst.CstType innerClass = (com.pranav.ide.dx.rop.cst.CstType) pool.get(innerClassIdx);
            com.pranav.ide.dx.rop.cst.CstType outerClass = (com.pranav.ide.dx.rop.cst.CstType) pool.get0Ok(outerClassIdx);
            com.pranav.ide.dx.rop.cst.CstString name = (com.pranav.ide.dx.rop.cst.CstString) pool.get0Ok(nameIdx);
            list.set(i, innerClass, outerClass, name, accessFlags);
            if (observer != null) {
                observer.parsed(bytes, offset, 2,
                        "inner_class: " +
                                com.pranav.ide.dx.cf.direct.DirectClassFile.stringOrNone(innerClass));
                observer.parsed(bytes, offset + 2, 2,
                        "  outer_class: " +
                                com.pranav.ide.dx.cf.direct.DirectClassFile.stringOrNone(outerClass));
                observer.parsed(bytes, offset + 4, 2,
                        "  name: " +
                                com.pranav.ide.dx.cf.direct.DirectClassFile.stringOrNone(name));
                observer.parsed(bytes, offset + 6, 2,
                        "  access_flags: " +
                                AccessFlags.innerClassString(accessFlags));
            }
            offset += 8;
        }

        list.setImmutable();
        return new AttInnerClasses(list);
    }

    /**
     * Parses a {@code LineNumberTable} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute lineNumberTable(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset,
                                                                 int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        int count = bytes.getUnsignedShort(offset); // line_number_table_length

        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "line_number_table_length: " + com.pranav.ide.dx.util.Hex.u2(count));
        }

        offset += 2;
        length -= 2;

        if (length != (count * 4)) {
            throwBadLength((count * 4) + 2);
        }

        com.pranav.ide.dx.cf.code.LineNumberList list = new LineNumberList(count);

        for (int i = 0; i < count; i++) {
            int startPc = bytes.getUnsignedShort(offset);
            int lineNumber = bytes.getUnsignedShort(offset + 2);
            list.set(i, startPc, lineNumber);
            if (observer != null) {
                observer.parsed(bytes, offset, 4,
                        com.pranav.ide.dx.util.Hex.u2(startPc) + " " + lineNumber);
            }
            offset += 4;
        }

        list.setImmutable();
        return new AttLineNumberTable(list);
    }

    /**
     * Parses a {@code LocalVariableTable} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute localVariableTable(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset,
                                                                    int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        int count = bytes.getUnsignedShort(offset);

        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "local_variable_table_length: " + com.pranav.ide.dx.util.Hex.u2(count));
        }

        com.pranav.ide.dx.cf.code.LocalVariableList list = parseLocalVariables(
                bytes.slice(offset + 2, offset + length), cf.getConstantPool(),
                observer, count, false);
        return new AttLocalVariableTable(list);
    }

    /**
     * Parses a {@code LocalVariableTypeTable} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute localVariableTypeTable(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset,
                                                                        int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        int count = bytes.getUnsignedShort(offset);

        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "local_variable_type_table_length: " + com.pranav.ide.dx.util.Hex.u2(count));
        }

        com.pranav.ide.dx.cf.code.LocalVariableList list = parseLocalVariables(
                bytes.slice(offset + 2, offset + length), cf.getConstantPool(),
                observer, count, true);
        return new AttLocalVariableTypeTable(list);
    }

    /**
     * Parse the table part of either a {@code LocalVariableTable}
     * or a {@code LocalVariableTypeTable}.
     *
     * @param bytes     {@code non-null;} bytes to parse, which should <i>only</i>
     *                  contain the table data (no header)
     * @param pool      {@code non-null;} constant pool to use
     * @param count     {@code >= 0;} the number of entries
     * @param typeTable {@code true} iff this is for a type table
     * @return {@code non-null;} the constructed list
     */
    private com.pranav.ide.dx.cf.code.LocalVariableList parseLocalVariables(com.pranav.ide.dx.util.ByteArray bytes,
                                                                            com.pranav.ide.dx.rop.cst.ConstantPool pool, com.pranav.ide.dx.cf.iface.ParseObserver observer, int count,
                                                                            boolean typeTable) {
        if (bytes.size() != (count * 10)) {
            // "+ 2" is for the count.
            throwBadLength((count * 10) + 2);
        }

        com.pranav.ide.dx.util.ByteArray.MyDataInputStream in = bytes.makeDataInputStream();
        com.pranav.ide.dx.cf.code.LocalVariableList list = new LocalVariableList(count);

        try {
            for (int i = 0; i < count; i++) {
                int startPc = in.readUnsignedShort();
                int length = in.readUnsignedShort();
                int nameIdx = in.readUnsignedShort();
                int typeIdx = in.readUnsignedShort();
                int index = in.readUnsignedShort();
                com.pranav.ide.dx.rop.cst.CstString name = (com.pranav.ide.dx.rop.cst.CstString) pool.get(nameIdx);
                com.pranav.ide.dx.rop.cst.CstString type = (com.pranav.ide.dx.rop.cst.CstString) pool.get(typeIdx);
                com.pranav.ide.dx.rop.cst.CstString descriptor = null;
                com.pranav.ide.dx.rop.cst.CstString signature = null;

                if (typeTable) {
                    signature = type;
                } else {
                    descriptor = type;
                }

                list.set(i, startPc, length, name,
                        descriptor, signature, index);

                if (observer != null) {
                    observer.parsed(bytes, i * 10, 10, com.pranav.ide.dx.util.Hex.u2(startPc) +
                            ".." + com.pranav.ide.dx.util.Hex.u2(startPc + length) + " " +
                            com.pranav.ide.dx.util.Hex.u2(index) + " " + name.toHuman() + " " +
                            type.toHuman());
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("shouldn't happen", ex);
        }

        list.setImmutable();
        return list;
    }

    /**
     * Parses a {@code RuntimeInvisibleAnnotations} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute runtimeInvisibleAnnotations(com.pranav.ide.dx.cf.direct.DirectClassFile cf,
                                                                             int offset, int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }

        com.pranav.ide.dx.cf.direct.AnnotationParser ap =
                new com.pranav.ide.dx.cf.direct.AnnotationParser(cf, offset, length, observer);
        com.pranav.ide.dx.rop.annotation.Annotations annotations =
                ap.parseAnnotationAttribute(com.pranav.ide.dx.rop.annotation.AnnotationVisibility.BUILD);

        return new AttRuntimeInvisibleAnnotations(annotations, length);
    }

    /**
     * Parses a {@code RuntimeVisibleAnnotations} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute runtimeVisibleAnnotations(com.pranav.ide.dx.cf.direct.DirectClassFile cf,
                                                                           int offset, int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }

        com.pranav.ide.dx.cf.direct.AnnotationParser ap =
                new com.pranav.ide.dx.cf.direct.AnnotationParser(cf, offset, length, observer);
        Annotations annotations =
                ap.parseAnnotationAttribute(com.pranav.ide.dx.rop.annotation.AnnotationVisibility.RUNTIME);

        return new AttRuntimeVisibleAnnotations(annotations, length);
    }

    /**
     * Parses a {@code RuntimeInvisibleParameterAnnotations} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute runtimeInvisibleParameterAnnotations(com.pranav.ide.dx.cf.direct.DirectClassFile cf,
                                                                                      int offset, int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }

        com.pranav.ide.dx.cf.direct.AnnotationParser ap =
                new com.pranav.ide.dx.cf.direct.AnnotationParser(cf, offset, length, observer);
        com.pranav.ide.dx.rop.annotation.AnnotationsList list =
                ap.parseParameterAttribute(com.pranav.ide.dx.rop.annotation.AnnotationVisibility.BUILD);

        return new AttRuntimeInvisibleParameterAnnotations(list, length);
    }

    /**
     * Parses a {@code RuntimeVisibleParameterAnnotations} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute runtimeVisibleParameterAnnotations(com.pranav.ide.dx.cf.direct.DirectClassFile cf,
                                                                                    int offset, int length, com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }

        com.pranav.ide.dx.cf.direct.AnnotationParser ap =
                new AnnotationParser(cf, offset, length, observer);
        AnnotationsList list =
                ap.parseParameterAttribute(AnnotationVisibility.RUNTIME);

        return new AttRuntimeVisibleParameterAnnotations(list, length);
    }

    /**
     * Parses a {@code Signature} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute signature(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                           com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length != 2) {
            throwBadLength(2);
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        com.pranav.ide.dx.rop.cst.ConstantPool pool = cf.getConstantPool();
        int idx = bytes.getUnsignedShort(offset);
        com.pranav.ide.dx.rop.cst.CstString cst = (com.pranav.ide.dx.rop.cst.CstString) pool.get(idx);
        com.pranav.ide.dx.cf.iface.Attribute result = new AttSignature(cst);

        if (observer != null) {
            observer.parsed(bytes, offset, 2, "signature: " + cst);
        }

        return result;
    }

    /**
     * Parses a {@code SourceDebugExtesion} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute sourceDebugExtension(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                                      com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes().slice(offset, offset + length);
        com.pranav.ide.dx.rop.cst.CstString smapString = new com.pranav.ide.dx.rop.cst.CstString(bytes);
        com.pranav.ide.dx.cf.iface.Attribute result = new AttSourceDebugExtension(smapString);

        if (observer != null) {
            String decoded = smapString.getString();
            observer.parsed(bytes, offset, length, "sourceDebugExtension: " + decoded);
        }

        return result;
    }

    /**
     * Parses a {@code SourceFile} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute sourceFile(com.pranav.ide.dx.cf.direct.DirectClassFile cf, int offset, int length,
                                                            com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length != 2) {
            throwBadLength(2);
        }

        com.pranav.ide.dx.util.ByteArray bytes = cf.getBytes();
        com.pranav.ide.dx.rop.cst.ConstantPool pool = cf.getConstantPool();
        int idx = bytes.getUnsignedShort(offset);
        com.pranav.ide.dx.rop.cst.CstString cst = (CstString) pool.get(idx);
        com.pranav.ide.dx.cf.iface.Attribute result = new AttSourceFile(cst);

        if (observer != null) {
            observer.parsed(bytes, offset, 2, "source: " + cst);
        }

        return result;
    }

    /**
     * Parses a {@code Synthetic} attribute.
     */
    private com.pranav.ide.dx.cf.iface.Attribute synthetic(DirectClassFile cf, int offset, int length,
                                                           com.pranav.ide.dx.cf.iface.ParseObserver observer) {
        if (length != 0) {
            return throwBadLength(0);
        }

        return new AttSynthetic();
    }

    private com.pranav.ide.dx.cf.code.BootstrapMethodsList parseBootstrapMethods(ByteArray bytes, ConstantPool constantPool,
                                                                                 CstType declaringClass, int numMethods, int offset, int length, ParseObserver observer)
            throws ParseException {
        com.pranav.ide.dx.cf.code.BootstrapMethodsList methods = new BootstrapMethodsList(numMethods);
        for (int methodIndex = 0; methodIndex < numMethods; ++methodIndex) {
            if (length < 4) {
                throwTruncated();
            }

            int methodRef = bytes.getUnsignedShort(offset);
            int numArguments = bytes.getUnsignedShort(offset + 2);

            if (observer != null) {
                observer.parsed(bytes, offset, 2, "bootstrap_method_ref: " + com.pranav.ide.dx.util.Hex.u2(methodRef));
                observer.parsed(bytes, offset + 2, 2,
                        "num_bootstrap_arguments: " + com.pranav.ide.dx.util.Hex.u2(numArguments));
            }

            offset += 4;
            length -= 4;
            if (length < numArguments * 2) {
                throwTruncated();
            }

            com.pranav.ide.dx.cf.code.BootstrapMethodArgumentsList arguments = new BootstrapMethodArgumentsList(numArguments);
            for (int argIndex = 0; argIndex < numArguments; ++argIndex, offset += 2, length -= 2) {
                int argumentRef = bytes.getUnsignedShort(offset);
                if (observer != null) {
                    observer.parsed(bytes, offset, 2,
                            "bootstrap_arguments[" + argIndex + "]" + Hex.u2(argumentRef));
                }
                arguments.set(argIndex, constantPool.get(argumentRef));
            }
            arguments.setImmutable();
            Constant cstMethodRef = constantPool.get(methodRef);
            methods.set(methodIndex, declaringClass, (CstMethodHandle) cstMethodRef, arguments);
        }
        methods.setImmutable();

        if (length != 0) {
            throwBadLength(length);
        }

        return methods;
    }
}
