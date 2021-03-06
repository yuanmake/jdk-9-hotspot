/*
 * Copyright (c) 2009, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.vm.ci.code;

import jdk.vm.ci.code.CompilationResult.Call;
import jdk.vm.ci.code.CompilationResult.DataPatch;
import jdk.vm.ci.code.CompilationResult.Mark;
import jdk.vm.ci.code.DataSection.Data;
import jdk.vm.ci.meta.Constant;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.SpeculationLog;

/**
 * Access to code cache related details and requirements.
 */
public interface CodeCacheProvider {

    /**
     * Installs code for a given method based on a given compilation result without making it the
     * default implementation of the method.
     *
     * @param method a method implemented by the installed code
     * @param compResult the compilation result to be added
     * @param log the speculation log to be used
     * @param installedCode a predefined {@link InstalledCode} object to use as a reference to the
     *            installed code. If {@code null}, a new {@link InstalledCode} object will be
     *            created.
     * @return a reference to the ready-to-run code
     * @throws BailoutException if the code installation failed
     */
    default InstalledCode addCode(ResolvedJavaMethod method, CompilationResult compResult, SpeculationLog log, InstalledCode installedCode) {
        return installCode(new CompilationRequest(method), compResult, installedCode, log, false);
    }

    /**
     * Installs code for a given method based on a given compilation result and makes it the default
     * implementation of the method.
     *
     * @param method a method implemented by the installed code and for which the installed code
     *            becomes the default implementation
     * @param compResult the compilation result to be added
     * @return a reference to the ready-to-run code
     * @throws BailoutException if the code installation failed
     */
    default InstalledCode setDefaultCode(ResolvedJavaMethod method, CompilationResult compResult) {
        return installCode(new CompilationRequest(method), compResult, null, null, true);
    }

    /**
     * Installs code based on a given compilation result.
     *
     * @param compRequest details of the method compiled to produce {@code compResult} or
     *            {@code null} if the input to {@code compResult} was not a
     *            {@link ResolvedJavaMethod}
     * @param compResult the compilation result to be added
     * @param installedCode a pre-allocated {@link InstalledCode} object to use as a reference to
     *            the installed code. If {@code null}, a new {@link InstalledCode} object will be
     *            created.
     * @param log the speculation log to be used
     * @param isDefault specifies if the installed code should be made the default implementation of
     *            {@code compRequest.getMethod()}. The default implementation for a method is the
     *            code executed for standard calls to the method. This argument is ignored if
     *            {@code compRequest == null}.
     * @return a reference to the compiled and ready-to-run installed code
     * @throws BailoutException if the code installation failed
     */
    InstalledCode installCode(CompilationRequest compRequest, CompilationResult compResult, InstalledCode installedCode, SpeculationLog log, boolean isDefault);

    /**
     * Invalidates {@code installedCode} such that {@link InvalidInstalledCodeException} will be
     * raised the next time {@code installedCode} is
     * {@linkplain InstalledCode#executeVarargs(Object...) executed}.
     */
    void invalidateInstalledCode(InstalledCode installedCode);

    /**
     * Gets a name for a {@link Mark} mark.
     */
    default String getMarkName(Mark mark) {
        return String.valueOf(mark.id);
    }

    /**
     * Gets a name for the {@linkplain Call#target target} of a {@link Call}.
     */
    default String getTargetName(Call call) {
        return String.valueOf(call.target);
    }

    /**
     * Gets the register configuration to use when compiling a given method.
     */
    RegisterConfig getRegisterConfig();

    /**
     * Minimum size of the stack area reserved for outgoing parameters. This area is reserved in all
     * cases, even when the compiled method has no regular call instructions.
     *
     * @return the minimum size of the outgoing parameter area in bytes
     */
    int getMinimumOutgoingSize();

    /**
     * Determines if a {@link DataPatch} should be created for a given primitive constant that is
     * part of a {@link CompilationResult}. A data patch is always created for an object constant.
     */
    boolean needsDataPatch(JavaConstant constant);

    /**
     * Create a {@link Data} item for one or more {@link Constant Constants}, that can be used in a
     * {@link DataPatch}. If more than one {@link Constant} is given, then they are tightly packed
     * into a single {@link Data} item.
     */
    Data createDataItem(Constant... constants);

    /**
     * Gets a description of the target architecture.
     */
    TargetDescription getTarget();

    /**
     * Create a new speculation log for the target runtime.
     */
    SpeculationLog createSpeculationLog();

    /**
     * Returns the maximum absolute offset of a PC relative call to a given address from any
     * position in the code cache or -1 when not applicable. Intended for determining the required
     * size of address/offset fields.
     */
    long getMaxCallTargetOffset(long address);

    /**
     * Determines if debug info should also be emitted at non-safepoint locations.
     */
    boolean shouldDebugNonSafepoints();
}
