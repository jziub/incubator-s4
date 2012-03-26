package org.apache.s4.comm.tcp;

import org.apache.s4.base.RemoteEmitter;
import org.apache.s4.comm.topology.RemoteTopology;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TCPRemoteEmitter extends TCPEmitter implements RemoteEmitter {

    @Inject
    public TCPRemoteEmitter(RemoteTopology topology, @Named("tcp.partition.queue_size") int bufferSize,
            @Named("comm.retries") int retries, @Named("comm.retry_delay") int retryDelay,
            @Named("comm.timeout") int timeout) throws InterruptedException {
        super(topology, bufferSize, retries, retryDelay, timeout);
    }

}
