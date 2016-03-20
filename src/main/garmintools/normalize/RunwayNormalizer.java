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

package garmintools.normalize;

import garmintools.Proto;
import garmintools.sections.SectionManager;
import garmintools.wrappers.Runway;

public class RunwayNormalizer {
  private static final String PCL = "PCL";
  private static final String UNKNOWN_SURFACE = "UNKNOWN SURFACE";

  private final SectionManager manager;

  public RunwayNormalizer(SectionManager manager) {
    this.manager = manager;
  }

  public Runway normalize(Proto.Runway proto) {
    Runway.Builder builder = Runway.newBuilder();
    Proto.Runway.Builder protoBuilder = Proto.Runway.newBuilder(proto);

    String runwayNumberSuffixString;
    if (proto.hasRunwaySuffix()) {
      runwayNumberSuffixString = proto.getRunwaySuffix().name();
      protoBuilder.clearRunwaySuffix();
    } else {
      runwayNumberSuffixString = "";
    }
    builder.withRunwayNumberSuffix(
        manager.getRunwayNumberSuffixTable().getKey(runwayNumberSuffixString));

    if (proto.getRunwayLighting() == Proto.Runway.RunwayLighting.PILOT_CONTROLLED_LIGHTING) {
      builder.withLighting(manager.getRunwayLightingSection().lookupOrInsert(PCL));
    } else {
      String runwayLighting = proto.getRunwayLighting().name();
      runwayLighting = runwayLighting.replace('_', ' ');
      builder.withLighting(manager.getRunwayLightingSection().lookupOrInsert(runwayLighting));
    }
    protoBuilder.clearRunwayLighting();

    String runwaySurface;
    if (proto.hasRunwaySurface()) {
      runwaySurface = proto.getRunwaySurface().name();
      runwaySurface = runwaySurface.replace('_', ' ');
    } else {
      runwaySurface = UNKNOWN_SURFACE;
    }
    builder.withSurface(manager.getRunwaySurfaceSection().lookupOrInsert(runwaySurface));
    protoBuilder.clearRunwaySurface();

    builder.withRunway(protoBuilder.build());
    return builder.build();
  }

  public Proto.Runway denormalize(Runway runway) {
    Proto.Runway.Builder protoBuilder = Proto.Runway.newBuilder(runway.protoRunway);
    String runwayNumberSuffix = manager.getRunwayNumberSuffixTable().lookup(runway.runwayNumberSuffix);
    if (!runwayNumberSuffix.isEmpty()) {
      protoBuilder.setRunwaySuffix(Proto.Runway.RunwaySuffix.valueOf(runwayNumberSuffix));
    }

    String runwayLighting = manager.getRunwayLightingSection().lookup(runway.lighting);
    runwayLighting = runwayLighting.replace(' ', '_');
    if (runwayLighting.equals(PCL)) {
      protoBuilder.setRunwayLighting(Proto.Runway.RunwayLighting.PILOT_CONTROLLED_LIGHTING);
    } else {
      protoBuilder.setRunwayLighting(Proto.Runway.RunwayLighting.valueOf(runwayLighting));
    }

    String runwaySurface = manager.getRunwaySurfaceSection().lookup(runway.surface);
    if (!runwaySurface.equals(UNKNOWN_SURFACE)) {
      runwaySurface = runwaySurface.replace(' ', '_');
      protoBuilder.setRunwaySurface(Proto.Runway.RunwaySurface.valueOf(runwaySurface));
    }

    return protoBuilder.build();
  }
}
