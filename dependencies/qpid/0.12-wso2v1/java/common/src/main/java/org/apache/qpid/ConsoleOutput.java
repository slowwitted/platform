/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid;

import static org.apache.qpid.transport.util.Functions.str;

import java.nio.ByteBuffer;

import org.apache.qpid.transport.Sender;


/**
 * ConsoleOutput
 *
 * @author Rafael H. Schloming
 */

public class ConsoleOutput implements Sender<ByteBuffer>
{

    public void send(ByteBuffer buf)
    {
        System.out.println(str(buf));
    }

    public void flush()
    {
        // pass
    }

    public void close()
    {
        System.out.println("CLOSED");
    }

    public void setIdleTimeout(int i)
    {
        // TODO Auto-generated method stub
        
    }
    
    

}
