/*
 * Copyright (C) 2016 goblob
 *
 * This file is part of Goblob for Android.
 *
 * Goblob for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Goblob for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Goblob for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.goblob.covid.geolocation;

import android.location.Location;

public interface FileLogger {

    void write(Location loc) throws Exception;

    void annotate(String description, Location loc) throws Exception;

    String getName();

}
