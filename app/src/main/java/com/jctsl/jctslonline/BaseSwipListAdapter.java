package com.jctsl.jctslonline;

/**
 * Created by Tejasv on 02-01-2018.
 */

import android.widget.BaseAdapter;

public abstract class BaseSwipListAdapter extends BaseAdapter {

    public boolean getSwipEnableByPosition(int position){
        return true;
    }



}