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

package garmintools.main;

import garmintools.Proto;
import garmintools.Proto.NavigationData;
import garmintools.adapters.garmin.TableOfContentsGarminAdapter;
import garmintools.files.GarminNavigationDataFile;
import garmintools.files.NavigationDataFileFactory;
import garmintools.files.ProtoNavigationDataFile;
import garmintools.sections.SectionManager;
import garmintools.sections.TableOfContentsSection;
import garmintools.wrappers.TableOfContentsEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

public class NavDataTool {
  private final Logger logger = Logger.getLogger(getClass().getName());

  public static void main(String args[]) throws Exception {
    new NavDataTool(args).run();
  }

  public NavDataTool(String args[]) throws IOException {
    this.args = args;
  }

  private final String args[];

  private void printHelpAndExitIf(boolean condition) {
    if (condition) {
      System.out.println("NavDataTool print garmin.bin");
      System.out.println("NavDataTool decode garmin.bin proto.bin");
      System.out.println("NavDataTool encode proto.bin garmin.bin");
      System.out.println("NavDataTool parse openAIP-airports.json proto.bin");
      System.out.println("NavDataTool toc garmin.bin");
      System.exit(-1);
    }
  }

  private void run() throws Exception {
    printHelpAndExitIf(args.length == 0);
    switch (args[0].toLowerCase()) {
    case "print":
      printHelpAndExitIf(args.length != 2);
      printGarminFile(new File(args[1]));
      break;
    case "encode":
      printHelpAndExitIf(args.length != 3);
      encodeGarminFile(new File(args[1]), new File(args[2]));
      break;
    case "decode":
      printHelpAndExitIf(args.length != 3);
      decodeGarminFile(new File(args[1]), new File(args[2]));
      break;
    case "parse":
      printHelpAndExitIf(args.length != 3);
      parseOpenAIPFile(new File(args[1]), new File(args[2]));
      break;
    case "toc":
      printHelpAndExitIf(args.length != 2);
      printTableOfContents(new File(args[1]));
      break;
    default:
      printHelpAndExitIf(true);
    }
  }

  private void printGarminFile(File garminDataFile) throws IOException {
    InputStream inputStream = new FileInputStream(garminDataFile);
    GarminNavigationDataFile dataFile =
        new NavigationDataFileFactory().createFromGarmin(inputStream, garminDataFile.length());
    dataFile.printSections();
  }

  private void printTableOfContents(File garminDataFile) throws IOException {
    InputStream inputStream = new FileInputStream(garminDataFile);
    ByteStreams.skipFully(inputStream, TableOfContentsGarminAdapter.TABLE_OF_CONTENTS_OFFSET);
    SectionManager.GarminBuilder sectionManagerBuilder = new SectionManager.GarminBuilder();
    sectionManagerBuilder.readTableOfContents(inputStream, Ints.checkedCast(garminDataFile.length()));
    SectionManager sectionManager = sectionManagerBuilder.build();
    TableOfContentsSection tocSection = sectionManager.getTableOfContentsSection();
    for (TableOfContentsEntry entry : tocSection.getEntryMap().values()) {
      System.out.println(entry.toString());
    }
    inputStream.close();
  }

  private void encodeGarminFile(File protoFile, File garminDataFile) throws IOException {
    FileInputStream inputStream = new FileInputStream(protoFile);
    FileOutputStream outputStream = new FileOutputStream(garminDataFile);
    logger.info(String.format("Reading from %s", protoFile.getAbsolutePath()));
    NavigationData proto = NavigationData.parseFrom(inputStream);
    ProtoNavigationDataFile dataFile =
        new NavigationDataFileFactory().createFromProto(proto);
    logger.info(String.format("Writing to %s", garminDataFile.getAbsolutePath()));
    dataFile.writeToGarmin(outputStream);
  }

  private void decodeGarminFile(File garminDataFile, File protoFile) throws IOException {
    FileInputStream inputStream = new FileInputStream(garminDataFile);
    FileOutputStream outputStream = new FileOutputStream(protoFile);
    logger.info(String.format("Reading from %s", garminDataFile.getAbsolutePath()));
    GarminNavigationDataFile dataFile = new NavigationDataFileFactory()
        .createFromGarmin(inputStream, garminDataFile.length());
    logger.info(String.format("Write to %s", protoFile.getAbsolutePath()));
    Proto.NavigationData.Builder protoBuilder = Proto.NavigationData.newBuilder();
    dataFile.writeToProto(protoBuilder);
    protoBuilder.build().writeTo(outputStream);
  }

  private void parseOpenAIPFile(File openAIPFile, File protoFile) throws IOException {
    FileInputStream inputStream = new FileInputStream(openAIPFile);
    FileOutputStream outputStream = new FileOutputStream(protoFile);

    logger.info(String.format("Reading from %s", openAIPFile.getAbsolutePath()));
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    List<Airport> airports = objectMapper.readValue(inputStream,
        new TypeReference<List<Airport>>() {
        });
    OpenAIPNavigationDataFile dataFile = new NavigationDataFileFactory()
        .createFromOpenAIP(airports);

    logger.info(String.format("Write to %s", protoFile.getAbsolutePath()));
    Proto.NavigationData.Builder protoBuilder = Proto.NavigationData.newBuilder();
    dataFile.writeToProto(protoBuilder);
    protoBuilder.build().writeTo(outputStream);
  }
}
