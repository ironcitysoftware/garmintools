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

package garmintools.wrappers;

import garmintools.Proto;
import garmintools.keys.IndexForeignKey;

public class CommunicationFrequency {
  public final Proto.CommunicationFrequency protoCommunicationFrequency;
  public final IndexForeignKey frequencyType;

  private CommunicationFrequency(Builder builder) {
    this.protoCommunicationFrequency = builder.protoCommunicationFrequency;
    this.frequencyType = builder.frequencyType;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Proto.CommunicationFrequency protoCommunicationFrequency;
    private IndexForeignKey frequencyType;

    private Builder() { }

    public Builder withCommunicationFrequency(Proto.CommunicationFrequency proto) {
      this.protoCommunicationFrequency = proto;
      return this;
    }

    public Builder withFrequencyType(IndexForeignKey frequencyType) {
      this.frequencyType = frequencyType;
      return this;
    }

    public CommunicationFrequency build() { return new CommunicationFrequency(this); }
  }

  @Override
  public String toString() {
    return protoCommunicationFrequency.toString();
  }
}
