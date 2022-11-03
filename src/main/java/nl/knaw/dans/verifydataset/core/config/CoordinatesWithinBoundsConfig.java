/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.verifydataset.core.config;

public class CoordinatesWithinBoundsConfig {
    Long minX;
    Long maxX;
    Long minY;
    Long maxY;

    public Long getMinX() {
        return minX;
    }

    public void setMinX(Long minX) {
        this.minX = minX;
    }

    public Long getMaxX() {
        return maxX;
    }

    public void setMaxX(Long maxX) {
        this.maxX = maxX;
    }

    public Long getMinY() {
        return minY;
    }

    public void setMinY(Long minY) {
        this.minY = minY;
    }

    public Long getMaxY() {
        return maxY;
    }

    public void setMaxY(Long maxY) {
        this.maxY = maxY;
    }

    @Override
    public String toString() {
        return "CoordinatesWithinBoundsConfig{" +
            "minX=" + minX +
            ", maxX=" + maxX +
            ", minY=" + minY +
            ", maxY=" + maxY +
            '}';
    }
}
