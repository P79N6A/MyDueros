/*
 * *
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.seuic.zh.mydueros.sample.sdk.devicemodule.localaudioplayer.message;

import java.io.Serializable;

/**
 * Created by longyin01 on 17/10/23.
 */

public class AudioFileTag implements Serializable {

    public String audioId;
    public String title;
    public String artist;
    public String album;
    public String year;
    public String genre;

    public AudioFileTag() {
    }

    public AudioFileTag(String audioId,
                        String title,
                        String artist,
                        String album,
                        String year,
                        String genre) {
        this.audioId = audioId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.genre = genre;
    }
}
