/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rocketbase.vaadin.croppie;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import io.rocketbase.vaadin.croppie.model.CropPoints;
import io.rocketbase.vaadin.croppie.model.CroppieConfiguration;
import io.rocketbase.vaadin.croppie.model.SizeConfig;
import io.rocketbase.vaadin.croppie.model.ViewPortConfig;
import lombok.Getter;
import lombok.Setter;

@Tag("vaadin-croppie")
@NpmPackage(value = "croppie", version = "2.6.5")
// @JsModule("./mycroppie/croppie.js")
public class Croppie extends PolymerTemplate<CroppieModel> implements HasSize, HasStyle {

  private static final PropertyDescriptor<String, String> srcDescriptor =
      PropertyDescriptors.attributeWithDefault("src", "");
  private static final PropertyDescriptor<String, String> labelDescriptor =
      PropertyDescriptors.attributeWithDefault("label", "");

  private CroppieConfiguration config;
  @Getter private boolean attached = false;
  private boolean initPhase = true;

  /** size is automatically configured via CroppieConfig boundary/viewport */
  @Setter private boolean autoConfigSize = true;

  public Croppie(AbstractStreamResource src) {
    withSrc(src);
  }

  public Croppie(String src) {
    withSrc(src);
  }

  public Croppie withConfig(CroppieConfiguration config) {
    this.config = config;
    return this;
  }

  protected CroppieConfiguration getConfigInitialized() {
    if (this.config == null) {
      this.config = new CroppieConfiguration();
    }
    return this.config;
  }

  protected Croppie getAndUpdateConfig() {
    if (isAttached()) {
      updateConfig();
    }
    return this;
  }

  public Croppie withPoints(CropPoints points) {
    getConfigInitialized().setPoints(points);
    return getAndUpdateConfig();
  }

  public Croppie withZoom(Float zoom) {
    getConfigInitialized().setZoom(zoom);
    return getAndUpdateConfig();
  }

  /**
   * The outer container of the cropper
   *
   * <p>Default will default to the size of the container
   */
  public Croppie withBoundary(SizeConfig boundary) {
    getConfigInitialized().setBoundary(boundary);
    return getAndUpdateConfig();
  }

  /**
   * The inner container of the coppie. The visible part of the image
   *
   * <p>Default { width: 100, height: 100, type: 'square' }
   */
  public Croppie withViewport(ViewPortConfig viewport) {
    getConfigInitialized().setViewport(viewport);
    return getAndUpdateConfig();
  }

  /**
   * Enable or disable support for resizing the viewport area.
   *
   * <p>Default false
   */
  public Croppie withEnableResize(boolean enableResize) {
    getConfigInitialized();
    this.config.setEnableResize(enableResize);
    return getAndUpdateConfig();
  }

  public Croppie withEnabledOrientation(boolean enabledOrientation) {
    getConfigInitialized();
    this.config.setEnableOrientation(enabledOrientation);
    return getAndUpdateConfig();
  }

  /**
   * Enable zooming functionality. If set to false - scrolling and pinching would not zoom.
   *
   * <p>Default true
   */
  public Croppie withEnableZoom(boolean enableZoom) {
    getConfigInitialized();
    this.config.setEnableZoom(enableZoom);
    return getAndUpdateConfig();
  }

  /**
   * Enable or disable the ability to use the mouse wheel to zoom in and out on a croppie instance.
   *
   * <p>Default true
   */
  public Croppie withMouseWheelZoom(boolean mouseWheelZoom) {
    getConfigInitialized();
    this.config.setMouseWheelZoom(mouseWheelZoom);
    return getAndUpdateConfig();
  }

  /**
   * Hide or Show the zoom slider
   *
   * <p>Default true
   */
  public Croppie withShowZoomer(boolean showZoomer) {
    getConfigInitialized();
    this.config.setShowZoomer(showZoomer);
    return getAndUpdateConfig();
  }

  public Registration addCropListener(ComponentEventListener<CropEvent> listener) {
    return addListener(CropEvent.class, listener);
  }

  /**
   * Gets the image URL.
   *
   * @return the image URL
   */
  public String getSrc() {
    return get(srcDescriptor);
  }

  /**
   * Sets the image URL.
   *
   * @param src the image URL
   */
  public Croppie withSrc(String src) {
    set(srcDescriptor, src);
    return getAndUpdateConfig();
  }

  /**
   * Sets the image URL with the URL of the given {@link StreamResource}.
   *
   * @param src the resource value, not null
   */
  public Croppie withSrc(AbstractStreamResource src) {
    getElement().setAttribute("src", src);
    return getAndUpdateConfig();
  }

  public String getLabel() {
    return get(labelDescriptor);
  }

  public Croppie withLabel(String label) {
    set(labelDescriptor, label);
    return getAndUpdateConfig();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    attached = true;
    updateConfig();
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    attached = false;
    initPhase = false;
  }

  protected void updateConfig() {
    getModel().setCroppieOptions(config.getJsonString());
    if (autoConfigSize) {
      if (getHeight() == null || getHeight().isEmpty()) {
        // TODO: needs a better implementation
        int extraHeight = config.isShowZoomer() ? 58 : 0;
        if (getLabel() != null && !getLabel().isEmpty()) {
          extraHeight += 18;
        }
        if (config.getBoundary() != null) {
          setWidth(String.format("%dpx", config.getBoundary().getWidth()));
          setHeight(String.format("%dpx", config.getBoundary().getHeight() + extraHeight));
          return;
        }
        if (config.getViewport() != null) {
          setWidth(String.format("%dpx", config.getViewport().getWidth()));
          setHeight(String.format("%dpx", config.getViewport().getHeight() + extraHeight));
        }
      }
    }
  }

  @ClientCallable
  private void cropChanged(String points, String zoom) {
    CropEvent event =
        new CropEvent(this, !initPhase, CropPoints.parseArray(points), Float.parseFloat(zoom));
    fireEvent(event);
    initPhase = false;
  }
}
