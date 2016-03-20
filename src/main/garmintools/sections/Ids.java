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

package garmintools.sections;

public class Ids {
  public static final int DATA_LENGTH_SECTION = 0;
  public static final int ICAO_REGION_SECTION = 1;
  public static final int STRING_SECTION = 2;
  public static final int LANDING_FACILITY_IDENTIFIER_INDEX_SECTION = 3;
  public static final int LANDING_FACILITY_SECTION = 6;
  public static final int LANDING_FACILITY_DETAIL_SECTION = 9;
  public static final int COORDINATE_SYSTEM_SECTION = 41;
  public static final int RUNWAY_LIGHTING_SECTION = 47;
  public static final int RUNWAY_SURFACE_SECTION = 48;
  public static final int GENERIC_AIRPORT_STRING_SECTION1 = 49;
  public static final int LHTU_SECTION = 53;
  public static final int APPROACH_TYPE_SECTION = 54;
  public static final int LANDING_FACILITY_TYPE_SECTION = 55;
  public static final int AIRSPACE_ABBREVIATION_SECTION1 = 59;
  public static final int AIRSPACE_ABBREVIATION_SECTION2 = 60;
  public static final int INSTRUMENT_APPROACH_TYPE_SECTION = 62;
  public static final int NESTB_SECTION = 64;
  public static final int BRIEF_AIRSPACE_TYPE_SECTION1 = 67;
  public static final int RNAV_TYPE_SECTION = 70;
  public static final int GPS_APPROACH_TYPE_SECTION1 = 76;
  public static final int GPS_APPROACH_TYPE_SECTION2 = 77;
  // TODO freqs?  also one is lowercase of the other.
  public static final int GENERIC_AIRPORT_STRING_SECTION2 = 79;
  public static final int EXPANDED_AIRSPACE_ABBREVIATION_SECTION1 = 80;
  public static final int EXPANDED_AIRSPACE_ABBREVIATION_SECTION2 = 81;
  public static final int BRIEF_AIRSPACE_TYPE_SECTION2 = 87;

  public static final int MAX_SECTION_NUMBER = 94;

  public static final int RUNWAY_NUMBER_SUFFIX_TABLE = 10001;
  public static final int AIRSPACE_TABLE = 10002;

  public static final int METADATA_SECTION = 12000;
  public static final int TABLE_OF_CONTENTS_SECTION = 12001;
}
