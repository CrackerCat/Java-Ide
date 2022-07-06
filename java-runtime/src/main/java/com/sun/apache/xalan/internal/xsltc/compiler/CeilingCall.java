/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: CeilingCall.java,v 1.2.4.1 2005/09/01 11:59:19 pvedula Exp $
 */

package com.sun.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;

import com.sun.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import com.sun.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import com.sun.apache.bcel.internal.generic.INVOKESTATIC;
import com.sun.apache.bcel.internal.generic.InstructionList;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class CeilingCall extends FunctionCall {
    public CeilingCall(QName fname, Vector arguments) {
        super(fname, arguments);
    }

    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
        final ConstantPoolGen cpg = classGen.getConstantPool();
        final InstructionList il = methodGen.getInstructionList();
        argument(0).translate(classGen, methodGen);
        il.append(new INVOKESTATIC(cpg.addMethodref(MATH_CLASS,
                                                    "ceil", "(D)D")));
    }
}
