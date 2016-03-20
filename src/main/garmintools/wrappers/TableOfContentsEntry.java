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

public class TableOfContentsEntry {
  public final int sectionNumber;
  public final int fileOffset;
  public final int itemLength;
  public final int itemQuantity;
  public final int actualLength;

  private TableOfContentsEntry(Builder builder) {
    this.sectionNumber = builder.sectionNumber;
    this.fileOffset = builder.fileOffset;
    this.itemLength = builder.itemLength;
    this.itemQuantity = builder.itemQuantity;
    this.actualLength = builder.actualLength;
  }

  @Override
  public String toString() {
    String result = String.format("section %2d at offset %08x %06x * %06x = %06x",
        sectionNumber,
        fileOffset,
        itemLength,
        itemQuantity,
        itemLength * itemQuantity);
    if (itemLength * itemQuantity != actualLength) {
      result += String.format(" ; actual length %08x", actualLength);
    }
    return result;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(TableOfContentsEntry entry) {
    return new Builder(entry);
  }

  public static class Builder {
    private int sectionNumber;
    private int fileOffset;
    private int itemLength;
    private int itemQuantity;
    private int actualLength;

    private Builder() { }

    private Builder(TableOfContentsEntry entry) {
      this.sectionNumber = entry.sectionNumber;
      this.fileOffset = entry.fileOffset;
      this.itemLength = entry.itemLength;
      this.itemQuantity = entry.itemQuantity;
      this.actualLength = entry.actualLength;
    }

    public Builder setSectionNumber(int sectionNumber) { this.sectionNumber = sectionNumber; return this; }
    public Builder setFileOffset(int fileOffset) { this.fileOffset = fileOffset; return this; }
    public Builder setItemLength(int itemLength) { this.itemLength = itemLength; return this; }
    public Builder setItemQuantity(int itemQuantity) { this.itemQuantity = itemQuantity; return this; }
    public Builder setActualLength(int actualLength) { this.actualLength = actualLength; return this; }
    public TableOfContentsEntry build() { return new TableOfContentsEntry(this); }
  }
}
