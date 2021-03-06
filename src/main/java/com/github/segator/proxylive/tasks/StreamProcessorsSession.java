/*
 * The MIT License
 *
 * Copyright 2017 Isaac Aymerich <isaac.aymerich@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.segator.proxylive.tasks;

import com.github.segator.proxylive.ProxyLiveUtils;
import com.github.segator.proxylive.entity.ClientInfo;
import com.github.segator.proxylive.processor.HLSStreamProcessor;
import com.github.segator.proxylive.processor.IStreamProcessor;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author Isaac Aymerich <isaac.aymerich@gmail.com>
 */
@Service
public class StreamProcessorsSession {

    private final List<ClientInfo> clientInfoList;

    public StreamProcessorsSession() {
        clientInfoList = new ArrayList();
    }

    public synchronized ClientInfo addClientInfo(ClientInfo client) {
        if (!clientInfoList.contains(client)) {
            clientInfoList.add(client);
        }
        return clientInfoList.get(clientInfoList.indexOf(client));
    }

    public synchronized void removeClientInfo(ClientInfo client) {
        if (clientInfoList.contains(client)) {
            clientInfoList.remove(client);
        }
    }

    public synchronized List<ClientInfo> getClientInfoList() {
        return clientInfoList;
    }

    public synchronized ClientInfo manage(IStreamProcessor iStreamProcessor, HttpServletRequest request) {
        ClientInfo client = new ClientInfo();
        request.getQueryString();
        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(ProxyLiveUtils.getURL(request)).build().getQueryParams();
        String clientUser = parameters.getFirst("user");
        if (clientUser == null) {
            clientUser = "guest";
        }
        client.setClientUser(clientUser);
        client.setIp(ProxyLiveUtils.getRequestIP(request));
        client.setBrowserInfo(ProxyLiveUtils.getBrowserInfo(request));
        client = addClientInfo(client);
        if (!client.getStreams().contains(iStreamProcessor)) {
            client.getStreams().add(iStreamProcessor);
        }
        return client;
    }

    public synchronized void removeClientInfo(ClientInfo client, IStreamProcessor iStreamProcessor) {
        client.getStreams().remove(iStreamProcessor);
        if (client.getStreams().isEmpty()) {
            removeClientInfo(client);
        }
    }

    public synchronized HLSStreamProcessor getHLSStream(String clientIdentifier, String channel, String profile) {
        for (ClientInfo clientInfo : clientInfoList) {
            if (clientIdentifier.equals(clientInfo.getIp())) {
                for (IStreamProcessor streamProcessor : clientInfo.getStreams()) {
                    if (streamProcessor.getTask().getIdentifier().equals(channel + "_" + profile + "_HLS")) {
                        return (HLSStreamProcessor) streamProcessor;
                    }
                }
            }
        }
        return null;
    }
}
