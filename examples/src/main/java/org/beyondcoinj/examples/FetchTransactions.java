/*
 * Copyright 2012 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.beyondcoinj.examples;

import org.beyondcoinj.core.*;
import org.beyondcoinj.params.MainNetParams;
import org.beyondcoinj.params.TestNet3Params;
import org.beyondcoinj.store.BlockStore;
import org.beyondcoinj.store.MemoryBlockStore;
import org.beyondcoinj.utils.BriefLogFormatter;
import com.google.common.util.concurrent.ListenableFuture;
import sun.applet.Main;

import java.net.InetAddress;
import java.util.List;

/**
 * Downloads the given transaction and its dependencies from a peers memory pool then prints them out.
 */
public class FetchTransactions {
    public static void main(String[] args) throws Exception {
        BriefLogFormatter.init();
        System.out.println("Connecting to node");
        final NetworkParameters params = MainNetParams.get();

        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.start();
        peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        Sha256Hash txHash = Sha256Hash.wrap("ffd26b8e5212b442d9cd0241a53c4fd1381bb24937844eb31b839b31c23dd057");
        ListenableFuture<Transaction> future = peer.getPeerMempoolTransaction(txHash);
        System.out.println("Waiting for node to send us the requested transaction: " + txHash);
        Transaction tx = future.get();
        System.out.println(tx);

        System.out.println("Waiting for node to send us the dependencies ...");
        List<Transaction> deps = peer.downloadDependencies(tx).get();
        for (Transaction dep : deps) {
            System.out.println("Got dependency " + dep.getHashAsString());
        }

        System.out.println("Done.");
        peerGroup.stop();
    }
}
