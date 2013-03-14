/*
 * Copyright (C) 2013 DroidDriver committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.droiddriver.uiautomation;

import android.app.UiAutomation;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.droiddriver.InputInjector;
import com.google.android.droiddriver.Matcher;
import com.google.android.droiddriver.UiElement;
import com.google.android.droiddriver.actions.Action;
import com.google.android.droiddriver.base.AbstractUiElement;
import com.google.android.droiddriver.exceptions.ElementNotFoundException;
import com.google.android.droiddriver.util.Logs;
import com.google.android.droiddriver.util.TextUtils;
import com.google.common.base.Preconditions;

/**
 * A UiElement that is backed by the UiAutomation object.
 */
public class UiAutomationElement extends AbstractUiElement {

  private final UiAutomation uiAutomation;
  private final AccessibilityNodeInfo node;
  private final InputInjector injector;

  public UiAutomationElement(UiAutomation uiAutomation, AccessibilityNodeInfo node) {
    this.uiAutomation = Preconditions.checkNotNull(uiAutomation);
    this.node = Preconditions.checkNotNull(node);
    this.injector = new UiAutomationInputInjector(uiAutomation);
  }

  @Override
  public String getText() {
    return TextUtils.charSequenceToString(node.getText());
  }

  @Override
  public String getContentDescription() {
    return TextUtils.charSequenceToString(node.getContentDescription());
  }

  @Override
  public String getClassName() {
    return TextUtils.charSequenceToString(node.getClassName());
  }

  @Override
  public UiElement findElement(Matcher matcher) {
    int childCount = node.getChildCount();
    Log.d(Logs.TAG, "Looping through number of childs " + childCount);
    for (int i = 0; i < childCount; i++) {
      AccessibilityNodeInfo childNode = node.getChild(i);
      if (childNode == null) {
        Log.w(Logs.TAG, "Found null child node for node: " + node);
        continue;
      }
      UiElement element = UiAutomationDrivers.newUiAutomationElement(uiAutomation, childNode);
      if (matcher.matches(element)) {
        Log.d(Logs.TAG, "Found match: " + node.getChild(i));
        return element;
      } else {
        try {
          return element.findElement(matcher);
        } catch (ElementNotFoundException enfe) {
          // Do nothing. Continue searching.
        }
      }
    }
    throw new ElementNotFoundException("Could not find any matching element for selector: "
        + matcher);
  }

  @Override
  public boolean perform(Action action) {
    return action.perform(injector, this);
  }

  @Override
  public boolean isVisible() {
    return node.isVisibleToUser();
  }

  @Override
  public Rect getRect() {
    Rect rect = new Rect();
    node.getBoundsInScreen(rect);
    return rect;
  }
}
