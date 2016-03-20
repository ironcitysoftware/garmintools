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

import garmintools.Proto.NavigationData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.google.protobuf.TextFormat;

public class PrintProto {
  public static void main(String args[]) throws Exception {
    new PrintProto(args).run();
  }

  public PrintProto(String args[]) throws IOException {
    this.args = args;
  }

  private final String args[];

  private void printHelpAndExitIf(boolean condition) {
    if (condition) {
      System.out.println("PrintProto navdata.proto.bin");
      System.exit(-1);
    }
  }

  private void run() throws Exception {
    printHelpAndExitIf(args.length == 0);
    NavigationData navData = NavigationData.parseFrom(new FileInputStream(new File(args[0])));
    TextFormat.print(navData, System.out);
  }
}
