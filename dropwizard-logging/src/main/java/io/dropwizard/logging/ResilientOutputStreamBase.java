/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package io.dropwizard.logging;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.recovery.RecoveryCoordinator;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Imported from Logback 1.2.3.
 *
 * @see ch.qos.logback.core.recovery.ResilientOutputStreamBase
 * @see <a href="https://github.com/qos-ch/logback/blob/v_1.2.3/logback-core/src/main/java/ch/qos/logback/core/recovery/ResilientOutputStreamBase.java">ResilientOutputStreamBase</a>
 */
@SuppressWarnings("NullAway")
abstract class ResilientOutputStreamBase extends OutputStream {

    private final static int STATUS_COUNT_LIMIT = 2 * 4;

    private int noContextWarning = 0;
    private int statusCount = 0;

    private Context context;
    private RecoveryCoordinator recoveryCoordinator;

    protected OutputStream os;
    boolean presumedClean = true;

    private boolean isPresumedInError() {
        // existence of recoveryCoordinator indicates failed state
        return (recoveryCoordinator != null && !presumedClean);
    }

    public void write(byte[] b, int off, int len) {
        if (isPresumedInError()) {
            if (!recoveryCoordinator.isTooSoon()) {
                attemptRecovery();
            }
            return; // return regardless of the success of the recovery attempt
        }

        try {
            os.write(b, off, len);
            postSuccessfulWrite();
        } catch (IOException e) {
            postIOFailure(e);
        }
    }

    @Override
    public void write(int b) {
        if (isPresumedInError()) {
            if (!recoveryCoordinator.isTooSoon()) {
                attemptRecovery();
            }
            return; // return regardless of the success of the recovery attempt
        }
        try {
            os.write(b);
            postSuccessfulWrite();
        } catch (IOException e) {
            postIOFailure(e);
        }
    }

    @Override
    public void flush() {
        if (os != null) {
            try {
                os.flush();
                postSuccessfulWrite();
            } catch (IOException e) {
                postIOFailure(e);
            }
        }
    }

    abstract String getDescription();

    abstract OutputStream openNewOutputStream() throws IOException;

    private void postSuccessfulWrite() {
        if (recoveryCoordinator != null) {
            recoveryCoordinator = null;
            statusCount = 0;
            addStatus(new InfoStatus("Recovered from IO failure on " + getDescription(), this));
        }
    }

    private void postIOFailure(IOException e) {
        addStatusIfCountNotOverLimit(new ErrorStatus("IO failure while writing to " + getDescription(), this, e));
        presumedClean = false;
        if (recoveryCoordinator == null) {
            recoveryCoordinator = new RecoveryCoordinator();
        }
    }

    @Override
    public void close() throws IOException {
        if (os != null) {
            os.close();
        }
    }

    private void attemptRecovery() {
        try {
            close();
        } catch (IOException e) {
            // Ignored
        }

        addStatusIfCountNotOverLimit(new InfoStatus("Attempting to recover from IO failure on " + getDescription(), this));

        // subsequent writes must always be in append mode
        try {
            os = openNewOutputStream();
            presumedClean = true;
        } catch (IOException e) {
            addStatusIfCountNotOverLimit(new ErrorStatus("Failed to open " + getDescription(), this, e));
        }
    }

    private void addStatusIfCountNotOverLimit(Status s) {
        ++statusCount;
        if (statusCount < STATUS_COUNT_LIMIT) {
            addStatus(s);
        }

        if (statusCount == STATUS_COUNT_LIMIT) {
            addStatus(s);
            addStatus(new InfoStatus("Will supress future messages regarding " + getDescription(), this));
        }
    }

    private void addStatus(Status status) {
        if (context == null) {
            if (noContextWarning++ == 0) {
                System.out.println("LOGBACK: No context given for " + this);
            }
            return;
        }
        StatusManager sm = context.getStatusManager();
        if (sm != null) {
            sm.add(status);
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
