package org.fourthline.cling.support.qplay;

import com.zxt.dlna.dmr.ZxtMediaPlayer;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.binding.annotations.UpnpStateVariables;
import org.fourthline.cling.binding.xml.Descriptor;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeDelegator;

import java.beans.PropertyChangeSupport;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Description: QPlay服务 <br>
 *
 * @author: fy <br>
 * Date: 2024/2/20 <br>
 */
@UpnpService(
        serviceId = @UpnpServiceId(namespace = UDAServiceId.QPLAY_NAMESPACE, value = "QPlay"),
        serviceType = @UpnpServiceType(value = "QPlay", version = 2),
        stringConvertibleTypes = LastChange.class
)
@UpnpStateVariables({
        @UpnpStateVariable(name = "A_ARG_TYPE_Seed",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(name = "A_ARG_TYPE_Code",
                sendEvents = false,
                datatype = "string",
                defaultValue = "0"),
        @UpnpStateVariable(name = "A_ARG_TYPE_MID",
                sendEvents = false,
                datatype = "string",
                defaultValue = "0"),
        @UpnpStateVariable(name = "A_ARG_TYPE_DID",
                sendEvents = false,
                datatype = "string",
                defaultValue = "0"),
        @UpnpStateVariable(name = "A_ARG_TYPE_TracksMetaData",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(name = "A_ARG_TYPE_NumberOfTracks",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(name = "A_ARG_TYPE_NextIndex",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(name = "A_ARG_TYPE_StartingIndex",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(name = "A_ARG_TYPE_QueueID",
                sendEvents = false,
                datatype = "string"),

})
public abstract class AbstractQPlayService implements LastChangeDelegator{
        @UpnpStateVariable(eventMaximumRateMilliseconds = 200)
    private LastChange lastChange;
    protected PropertyChangeSupport propertyChangeSupport;

    public AbstractQPlayService() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    protected AbstractQPlayService(LastChange lastChange) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = lastChange;
    }

    protected AbstractQPlayService(PropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport;
    }

    @Override
    public LastChange getLastChange() {
        return lastChange;
    }

    public abstract ZxtMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws AVTransportException;

    @Override
    public void appendCurrentState(LastChange lc, UnsignedIntegerFourBytes instanceId) throws Exception {
        QPlayAuth qPlayAuth = getInstance(instanceId).getqPlayAuth();
        lc.setEventedValue(
                instanceId,
                new QPlayTransportVariable.QPlayCode(qPlayAuth.getCode()),
                new QPlayTransportVariable.QPlayDid(qPlayAuth.getDID()),
                new QPlayTransportVariable.QPlayMid(qPlayAuth.getMID())
        );
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    /**
     * QPlay认证
     */
    @UpnpAction(out = {
            @UpnpOutputArgument(name = "Code", stateVariable = "A_ARG_TYPE_Code",getterName = "getCode"),
            @UpnpOutputArgument(name = "MID", stateVariable = "A_ARG_TYPE_MID",getterName = "getMID"),
            @UpnpOutputArgument(name = "DID", stateVariable = "A_ARG_TYPE_DID",getterName = "getDID")
    })
    public abstract QPlayAuth qPlayAuth(@UpnpInputArgument(name = "Seed", stateVariable = "A_ARG_TYPE_Seed")
                                        String seed) throws AVTransportException, NoSuchAlgorithmException;

    /**
     * 获取曲目
     */
    @UpnpAction(out = {
            @UpnpOutputArgument(name = "TracksMetaData ", stateVariable = "A_ARG_TYPE_TracksMetaData")
    })
    public abstract String getTracksInfo(@UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_StartingIndex")
                                         String startIndex,
                                         @UpnpInputArgument(name = "NumberOfTracks", stateVariable = "A_ARG_TYPE_NumberOfTracks")
                                         String number) throws ActionException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "NrTracks", stateVariable = "A_ARG_TYPE_NumberOfTracks")
    })
    public abstract String getTracksCount() throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "NumberOfSuccess", stateVariable = "A_ARG_TYPE_NumberOfTracks")
    })

    public abstract String setTracksInfo(@UpnpInputArgument(name = "QueueID", stateVariable = "A_ARG_TYPE_QueueID")
                                         String queueId,
                                         @UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_StartingIndex")
                                         String startIndex,
                                         @UpnpInputArgument(name = "NextIndex", stateVariable = "A_ARG_TYPE_NextIndex")
                                         String nextIndex,
                                         @UpnpInputArgument(name = "TracksMetaData", stateVariable = "A_ARG_TYPE_TracksMetaData")
                                         String tracksMetaData) throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "NumberOfSuccess", stateVariable = "A_ARG_TYPE_NumberOfTracks")
    })
    public abstract String insertTracks(@UpnpInputArgument(name = "QueueID", stateVariable = "A_ARG_TYPE_QueueID")
                                        String queueId,
                                        @UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_StartingIndex")
                                        String startIndex,
                                        @UpnpInputArgument(name = "TracksMetaData", stateVariable = "A_ARG_TYPE_TracksMetaData")
                                        String tracksMetaData) throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "NumberOfSuccess", stateVariable = "A_ARG_TYPE_NumberOfTracks")
    })
    public abstract String removeTracks(@UpnpInputArgument(name = "QueueID", stateVariable = "A_ARG_TYPE_QueueID")
                                        String queueId,
                                        @UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_StartingIndex")
                                        String startIndex,
                                        @UpnpInputArgument(name = "NumberOfTracks", stateVariable = "A_ARG_TYPE_NumberOfTracks")
                                        String number) throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "GetMaxTracks", stateVariable = "A_ARG_TYPE_NumberOfTracks")
    })
    public abstract String getMaxTracks() throws ActionException;

}
