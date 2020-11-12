package com.hcanyz.environmentvariable;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Set;

public interface IEvManager {

    String getEvItemCurrentValue(@NonNull String key);

    Set<String> getFullVariants();

    List<EvHolder> getEvHolders(@NonNull Context context);
}
