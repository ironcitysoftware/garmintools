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

package garmintools.encoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.google.common.io.ByteArrayDataOutput;

/** Adapts a ByteArrayDataOutput to be little-endian. */
public class LittleEndianByteArrayDataOutput implements ByteArrayDataOutput {
  private final ByteArrayDataOutput output;

  public LittleEndianByteArrayDataOutput(ByteArrayDataOutput output) {
    this.output = output;
  }

  @Override
  public byte[] toByteArray() {
    return output.toByteArray();
  }

  @Override
  public void write(int data) {
    output.write(data);
  }

  @Override
  public void write(byte[] data) {
    output.write(data);
  }

  @Override
  public void write(byte[] data, int off, int len) {
    output.write(data, off, len);
  }

  @Override
  public void writeBoolean(boolean data) {
    output.writeBoolean(data);
  }

  @Override
  public void writeByte(int data) {
    output.writeByte(data);
  }

  @Override
  @Deprecated
  public void writeBytes(String data) {
    output.writeBytes(data);
  }

  @Override
  public void writeChar(int data) {
    output.writeChar(data);
  }

  @Override
  public void writeChars(String data) {
    output.writeChars(data);
  }

  @Override
  public void writeDouble(double data) {
    ByteBuffer buffer = ByteBuffer.allocate(Double.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putDouble(data);
    output.write(buffer.array());
  }

  @Override
  public void writeFloat(float data) {
    ByteBuffer buffer = ByteBuffer.allocate(Float.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putFloat(data);
    output.write(buffer.array());
  }

  @Override
  public void writeInt(int data) {
    ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(data);
    output.write(buffer.array());
  }

  @Override
  public void writeLong(long data) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putLong(data);
    output.write(buffer.array());
  }

  @Override
  public void writeShort(int data) {
    ByteBuffer buffer = ByteBuffer.allocate(Short.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putShort((short) data);
    output.write(buffer.array());
  }

  @Override
  public void writeUTF(String data) {
    output.writeUTF(data);
  }
}
