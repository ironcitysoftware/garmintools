/**
 *    Copyright 2016 Iron City Software LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package garmintools.adapters.nativo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;

public class NativeOutput {
  private final int itemQuantity;
  private final int itemLength;
  private final ByteBuffer byteBuffer;

  public NativeOutput(int itemQuantity, int itemLength) {
    this.itemQuantity = itemQuantity;
    this.itemLength = itemLength;
    this.byteBuffer = ByteBuffer.allocate(itemQuantity * itemLength).order(ByteOrder.LITTLE_ENDIAN);
  }

  public NativeOutput(int itemQuantity, int itemLength, int actualSize) {
    this.itemQuantity = itemQuantity;
    this.itemLength = itemLength;
    this.byteBuffer = ByteBuffer.allocate(actualSize).order(ByteOrder.LITTLE_ENDIAN);
  }

  public NativeOutput(byte data[]) {
    this.itemQuantity = data.length;
    this.itemLength = 1;
    this.byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer.position(byteBuffer.limit());
  }

  public void put(byte data) {
    byteBuffer.put(data);
  }

  public void put(byte data[]) {
    byteBuffer.put(data);
  }

  public void put(ByteBuffer data) {
    byteBuffer.put(data);
  }

  public void putShort(short data) {
    byteBuffer.putShort(data);
  }

  public void putInt(int data) {
    byteBuffer.putInt(data);
  }

  public int size() {
    return byteBuffer.limit();
  }

  public int getItemLength() {
    return itemLength;
  }

  public int getItemQuantity() {
    return itemQuantity;
  }

  public void write(WritableByteChannel channel) throws IOException {
    byteBuffer.flip();
    channel.write(byteBuffer);
  }
}