/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.fourthline.cling.support.qplay;

import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.lastchange.Event;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.EventedValueString;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Bauer
 */
public class QPlayTransportVariable {

    public static Set<Class<? extends EventedValue>> ALL = new HashSet<Class<? extends EventedValue>>() {{
        add(SetNetwork.class);
        add(QPlayAuth.class);
        add(InsertTracks.class);
        add(RemoveTracks.class);
        add(GetTracksInfo.class);
        add(SetTracksInfo.class);
        add(GetTracksCount.class);
        add(GetMaxTracks.class);
    }};


    public static class QPlayCode extends EventedValueString {
        public QPlayCode(String value) {
            super(value);
        }

        public QPlayCode(Map.Entry<String, String>[] attrs) {
            super(attrs);
        }
    }

    public static class QPlayMid extends EventedValueString {
        public QPlayMid(String value) {
            super(value);
        }

        public QPlayMid(Map.Entry<String, String>[] attrs) {
            super(attrs);
        }
    }

    public static class QPlayDid extends EventedValueString {
        public QPlayDid(String value) {
            super(value);
        }

        public QPlayDid(Map.Entry<String, String>[] attrs) {
            super(attrs);
        }
    }

    public static class SetNetwork extends EventedValueString {
        public SetNetwork(String value) {
            super(value);
        }

        public SetNetwork(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    public static class QPlayAuth extends EventedValue<org.fourthline.cling.support.qplay.QPlayAuth> {
        public QPlayAuth(org.fourthline.cling.support.qplay.QPlayAuth value) {
            super(value);
        }

        public QPlayAuth(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        @Override
        protected Datatype<String> getDatatype() {
                return Datatype.Default.STRING.getBuiltinType().getDatatype();
        }
    }

    public static class InsertTracks extends EventedValueString {
        public InsertTracks(String value) {
            super(value);
        }

        public InsertTracks(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    public static class RemoveTracks extends EventedValueString {
        public RemoveTracks(String value) {
            super(value);
        }

        public RemoveTracks(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    public static class GetTracksInfo extends EventedValueString {
        public GetTracksInfo(String value) {
            super(value);
        }

        public GetTracksInfo(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    public static class SetTracksInfo extends EventedValueString {
        public SetTracksInfo(String value) {
            super(value);
        }

        public SetTracksInfo(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    public static class GetTracksCount extends EventedValueString {
        public GetTracksCount(String value) {
            super(value);
        }

        public GetTracksCount(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    public static class GetMaxTracks extends EventedValueString {
        public GetMaxTracks(String value) {
            super(value);
        }

        public GetMaxTracks(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

}
