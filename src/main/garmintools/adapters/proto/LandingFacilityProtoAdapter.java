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

package garmintools.adapters.proto;

import garmintools.Proto;
import garmintools.Proto.NavigationData;
import garmintools.wrappers.LandingFacility;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class LandingFacilityProtoAdapter implements ProtoAdapter<List<LandingFacility>> {
  @Override
  public List<LandingFacility> read(NavigationData navData) {
    ImmutableList.Builder<LandingFacility> listBuilder = ImmutableList.builder();
    for (Proto.LandingFacility landingFacility : navData.getLandingFacilityList()) {
      listBuilder.add(LandingFacility.newBuilder().withLandingFacility(landingFacility).build());
    }
    return listBuilder.build();
  }

  @Override
  public void write(List<LandingFacility> landingFacilities, NavigationData.Builder builder) {
    for (LandingFacility landingFacility : landingFacilities) {
      builder.addLandingFacility(landingFacility.protoLandingFacility);
    }
  }
}
