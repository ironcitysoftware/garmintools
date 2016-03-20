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

public class Runway {
  public final Proto.Runway protoRunway;
  public final IndexForeignKey runwayNumberSuffix;
  public final IndexForeignKey lighting;
  public final IndexForeignKey surface;

  private Runway(Builder builder) {
    this.protoRunway = builder.protoRunway;
    this.runwayNumberSuffix = builder.runwayNumberSuffix;
    this.lighting = builder.lighting;
    this.surface = builder.surface;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Proto.Runway protoRunway;
    private IndexForeignKey runwayNumberSuffix;
    private IndexForeignKey lighting;
    private IndexForeignKey surface;

    private Builder() { }
    public Builder withRunway(Proto.Runway proto) { this.protoRunway = proto; return this; }
    public Builder withRunwayNumberSuffix(IndexForeignKey key) { this.runwayNumberSuffix = key; return this; }
    public Builder withLighting(IndexForeignKey key) { this.lighting = key; return this; }
    public Builder withSurface(IndexForeignKey key) { this.surface = key; return this; }
    public Runway build() { return new Runway(this); }
  }

  @Override
  public String toString() {
    return protoRunway.toString();
  }
}
