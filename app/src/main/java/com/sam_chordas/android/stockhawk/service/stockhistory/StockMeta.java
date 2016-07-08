/*
 * Copyright 2016 Rohit Sharma (skyrohithigh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sam_chordas.android.stockhawk.service.stockhistory;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by rohit on 7/8/16.
 */
public class StockMeta implements Parcelable {
    public String companyName;
    public String exchangeName;
    public String firstTrade;
    public String lastTrade;
    public String currency;
    public double previousClosePrice;
    public ArrayList<StockSymbol> stockSymbols;

    public StockMeta(String companyName, String exchangeName, String firstTrade, String lastTrade, String currency, double previousClosePrice, ArrayList<StockSymbol> stockSymbols) {
        this.companyName = companyName;
        this.exchangeName = exchangeName;
        this.firstTrade = firstTrade;
        this.lastTrade = lastTrade;
        this.currency = currency;
        this.previousClosePrice = previousClosePrice;
        this.stockSymbols = stockSymbols;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.companyName);
        dest.writeString(this.exchangeName);
        dest.writeString(this.firstTrade);
        dest.writeString(this.lastTrade);
        dest.writeString(this.currency);
        dest.writeDouble(this.previousClosePrice);
        dest.writeTypedList(this.stockSymbols);
    }

    protected StockMeta(Parcel in) {
        this.companyName = in.readString();
        this.exchangeName = in.readString();
        this.firstTrade = in.readString();
        this.lastTrade = in.readString();
        this.currency = in.readString();
        this.previousClosePrice = in.readDouble();
        this.stockSymbols = in.createTypedArrayList(StockSymbol.CREATOR);
    }

    public static final Parcelable.Creator<StockMeta> CREATOR = new Parcelable.Creator<StockMeta>() {
        @Override
        public StockMeta createFromParcel(Parcel source) {
            return new StockMeta(source);
        }

        @Override
        public StockMeta[] newArray(int size) {
            return new StockMeta[size];
        }
    };
}
