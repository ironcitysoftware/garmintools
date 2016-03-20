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

package garmintools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class StringUtilTest {
  @Test
  public void testPad_TooShort() {
    try {
      StringUtil.pad("ABCD", 2);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void testPad_ExactLength() {
    assertEquals("ABCD", StringUtil.pad("ABCD", 4));
  }

  @Test
  public void testPad() {
    assertEquals("ABCD  ", StringUtil.pad("ABCD", 6));
  }
}
