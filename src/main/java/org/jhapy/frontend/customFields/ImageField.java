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

package org.jhapy.frontend.customFields;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.io.IOUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.component.cropperjs.CropperConfiguration;
import org.jhapy.frontend.component.cropperjs.CropperJs;
import org.jhapy.frontend.component.cropperjs.model.Data;
import org.jhapy.frontend.component.cropperjs.model.DragMode;
import org.jhapy.frontend.component.cropperjs.model.ViewMode;
import org.jhapy.frontend.components.AbstractDialog;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.UIUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-13
 */
public class ImageField extends CustomField<StoredFile> implements HasStyle, Serializable,
    HasLogger {

  private final Image image;
  private StoredFile storedFile;
  private AbstractDialog uploadDialog;
  private CropperJs cropperJs;
  private int nbAddedFile = 0;

  public ImageField() {
    this(null);
  }

  private FlexBoxLayout contentLayout;

  public ImageField(String label) {
    if (label != null) {
      setLabel(label);
    }

    FlexBoxLayout contentLayout = new FlexBoxLayout();
    contentLayout.setSizeFull();
    contentLayout.setJustifyContentMode(JustifyContentMode.CENTER);

    image = new Image(AppConst.NO_PICTURE, "");
    image.setHeight("150px");
    image.addClickListener(e -> addOrUpdateImage());

    contentLayout.add(image);

    add(contentLayout);
  }

  private void addOrUpdateImage() {
    var loggerPrefix = getLoggerPrefix("addOrUpdateImage");

    MemoryBuffer buffer = new MemoryBuffer();
    Upload upload = new Upload(buffer);
    upload.setAcceptedFileTypes("image/*");
    upload.setAutoUpload(true);
    upload.setMaxFiles(1);

    uploadDialog = new AbstractDialog() {

      @Override
      protected String getTitle() {
        return getTranslation("element.global.addOrUpdate");
      }

      @Override
      protected Component getContent() {
        contentLayout = new FlexBoxLayout();
        contentLayout.setWidthFull();
        contentLayout.setHeightFull();
        contentLayout.setFlexDirection(FlexDirection.COLUMN);

        contentLayout.add(upload);

        if (storedFile != null) {
          if (storedFile.getOrginalContent() == null) {
            storedFile.setOrginalContent(storedFile.getContent());
          }
          buildCropper(storedFile.getOrginalContent(), storedFile.getFilename(),
              storedFile != null ? storedFile.getMetadata().get("copperData") : null);
        }

        return contentLayout;
      }

      @Override
      protected List<Component> getButtons() {
        Button delete = UIUtils
            .createTertiaryButton(getTranslation("action.global.delete"));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        delete.addClickListener(event -> {
          storedFile = null;
          image.setSrc(AppConst.NO_PICTURE);
          updateValue();
          close();
        });
        return Collections.singletonList(delete);
      }

      @Override
      protected boolean onSave() {
        cropperJs.getData(true, data -> {
          var loggerPrefix = getLoggerPrefix("save");

          logger().debug(loggerPrefix + "Data = " + data);

          javaxt.io.Image imagext = new javaxt.io.Image(storedFile.getOrginalContent());
          //dumpImageInfo(imagext, loggerPrefix);

          if (data.getRotate() != 0) {
            imagext.rotate(data.getRotate().intValue());
          }

          if (data.getScaleY() != 1 && data.getScaleY() != 1) {
            imagext.flip();
          }

          imagext.crop(data.getX().intValue(), data.getY().intValue(),
              data.getWidth().intValue(),
              data.getHeight().intValue());

          storedFile.getMetadata().put("copperData", data.getJsonString());
          storedFile.setContent(imagext.getByteArray());
          storedFile.setFilename(storedFile.getFilename());
          storedFile.setFilesize((long) storedFile.getContent().length);
          storedFile.setHasChanged(true);

          image.setSrc(new StreamResource(storedFile.getFilename(),
              () -> new ByteArrayInputStream(storedFile.getContent())));
          updateValue();
          cropperJs.destroy();
          close();

        });
        return false;
      }

      private void dumpImageInfo(javaxt.io.Image image, String loggerPrefix) {
        java.util.HashMap<Integer, Object> exif = image.getExifTags();

        logger().debug(loggerPrefix + "EXIF Fields: " + exif.size());
        logger().debug(loggerPrefix + "-----------------------------");
        logger().debug(loggerPrefix + "Date: " + exif.get(0x0132)); //0x9003
        logger().debug(loggerPrefix + "Camera: " + exif.get(0x0110));
        logger().debug(loggerPrefix + "Manufacturer: " + exif.get(0x010F));
        logger().debug(loggerPrefix + "Focal Length: " + exif.get(0x920A));
        logger().debug(loggerPrefix + "F-Stop: " + exif.get(0x829D));
        logger()
            .debug(loggerPrefix + "Exposure Time (1 / Shutter Speed): " + exif.get(0x829A));
        logger().debug(loggerPrefix + "ISO Speed Ratings: " + exif.get(0x8827));
        logger().debug(loggerPrefix + "Shutter Speed Value (APEX): " + exif.get(0x9201));
        logger().debug(loggerPrefix + "Shutter Speed (Exposure Time): " + exif.get(0x9201));
        logger().debug(loggerPrefix + "Aperture Value (APEX): " + exif.get(0x9202));

        //Print Image Orientation
        try {
          int orientation = (Integer) exif.get(0x0112);
          String desc = switch (orientation) {
            case 1 -> "Top, left side (Horizontal / normal)";
            case 2 -> "Top, right side (Mirror horizontal)";
            case 3 -> "Bottom, right side (Rotate 180)";
            case 4 -> "Bottom, left side (Mirror vertical)";
            case 5 -> "Left side, top (Mirror horizontal and rotate 270 CW)";
            case 6 -> "Right side, top (Rotate 90 CW)";
            case 7 -> "Right side, bottom (Mirror horizontal and rotate 90 CW)";
            case 8 -> "Left side, bottom (Rotate 270 CW)";
            default -> "";
          };
          logger().debug(loggerPrefix + "Orientation: " + orientation + " -- " + desc);
        } catch (Exception e) {
        }

        //Print GPS Information
        double[] coord = image.getGPSCoordinate();
        if (coord != null) {
          logger().debug(loggerPrefix + "GPS Coordinate: " + coord[0] + ", " + coord[1]);
          logger().debug(loggerPrefix + "GPS Datum: " + image.getGPSDatum());
        }

        java.util.HashMap<Integer, Object> iptc = image.getIptcTags();
        logger().debug(loggerPrefix + "IPTC Fields: " + iptc.size());
        logger().debug(loggerPrefix + "-----------------------------");
        logger().debug(loggerPrefix + "Date: " + iptc.get(0x0237));
        logger().debug(loggerPrefix + "Caption: " + iptc.get(0x0278));
        logger().debug(loggerPrefix + "Copyright: " + iptc.get(0x0274));

      }

      @Override
      protected boolean onClose() {
        if (storedFile != null) {
          image.setSrc(new StreamResource(storedFile.getFilename(),
              () -> new ByteArrayInputStream(storedFile.getContent())));
        } else {
          image.setSrc(AppConst.NO_PICTURE);
        }
        if (cropperJs != null) {
          cropperJs.destroy();
        }
        return true;
      }

      @Override
      protected void onDialogResized(DialogResizeEvent event) {
        if (nbAddedFile > 0) {
          int _height = (int) Double
              .parseDouble(
                  event.getHeight().substring(0, event.getHeight().indexOf("px")).trim());
          int _width = (int) Double
              .parseDouble(
                  event.getWidth().substring(0, event.getWidth().indexOf("px")).trim());
          cropperJs.resize(_height - 30, _width);
        } else {
          cropperJs.resize(event.getHeight(), event.getWidth());
        }
      }

      @Override
      public boolean canMaximize() {
        return false;
      }
    };

    upload.addSucceededListener(event -> {
      storedFile = new StoredFile();
      storedFile.setMimeType(event.getMIMEType());
      try {
        storedFile.setContent(IOUtils.toByteArray(buffer.getInputStream()));
      } catch (IOException e) {
        Notification.show(e.getLocalizedMessage());
      }
      storedFile.setOrginalContent(storedFile.getContent());
      storedFile.setFilesize((long) storedFile.getContent().length);
      storedFile.setFilename(buffer.getFileName());

      nbAddedFile++;

      if (cropperJs == null) {
        buildCropper(storedFile.getContent(), storedFile.getFilename(), null);
      } else {
        cropperJs.withSrc(new StreamResource(buffer.getFileName(),
            () -> new ByteArrayInputStream(storedFile.getContent())));
      }
    });

    uploadDialog.open();
  }

  @Override
  protected StoredFile generateModelValue() {
    return storedFile;
  }

  protected void buildCropper(byte[] content, String filename, String cropperData) {
    StreamResource imageResource = new StreamResource(filename,
        () -> new ByteArrayInputStream(content));

    cropperJs = new CropperJs(imageResource, true);
    cropperJs.setWidthFull();

    CropperConfiguration cropperConfiguration = new CropperConfiguration();
    cropperConfiguration.setAspectRatio(1f);
    cropperConfiguration.setViewMode(ViewMode.RESTRICT_TO_CANVAS);

    if (cropperData != null) {
      cropperConfiguration.setData(new Gson().fromJson(cropperData, Data.class));
    }

    cropperJs.withConfig(cropperConfiguration);

    contentLayout.add(cropperJs);

    FlexBoxLayout toolsLayout = new FlexBoxLayout();
    toolsLayout.setWidthFull();
    toolsLayout.setFlexWrap(FlexWrap.WRAP);
    toolsLayout.setJustifyContentMode(JustifyContentMode.EVENLY);
    toolsLayout.setFlexDirection(FlexDirection.ROW);

    Button moveButton = UIUtils.createSmallButton(VaadinIcon.ARROWS);
    moveButton.addClickListener(event -> cropperJs.setDragMode(DragMode.MOVE));
    Button cropButton = UIUtils.createSmallButton(VaadinIcon.CROP);
    cropButton.addClickListener(event -> cropperJs.setDragMode(DragMode.CROP));

    Button zoomOutButton = UIUtils.createSmallButton(VaadinIcon.SEARCH_MINUS);
    zoomOutButton.addClickListener(event -> cropperJs.zoom(-0.1f));
    Button zoomInButton = UIUtils.createSmallButton(VaadinIcon.SEARCH_PLUS);
    zoomInButton.addClickListener(event -> cropperJs.zoom(0.1f));

    Button moveLeftButton = UIUtils.createSmallButton(VaadinIcon.ARROW_LEFT);
    moveLeftButton.addClickListener(event -> cropperJs.move(-10f, 0f));
    Button moveRightButton = UIUtils.createSmallButton(VaadinIcon.ARROW_RIGHT);
    moveRightButton.addClickListener(event -> cropperJs.move(10f, 0f));
    Button moveUpButton = UIUtils.createSmallButton(VaadinIcon.ARROW_UP);
    moveUpButton.addClickListener(event -> cropperJs.move(0f, -10f));
    Button moveDownButton = UIUtils.createSmallButton(VaadinIcon.ARROW_DOWN);
    moveDownButton.addClickListener(event -> cropperJs.move(0f, 10));

    Button rotateLeftButton = UIUtils.createSmallButton(VaadinIcon.ROTATE_LEFT);
    rotateLeftButton.addClickListener(event -> cropperJs.rotate(-45));
    Button rotateRightButton = UIUtils.createSmallButton(VaadinIcon.ROTATE_RIGHT);
    rotateRightButton.addClickListener(event -> cropperJs.rotate(45));

    final AtomicInteger scaleX = new AtomicInteger(-1);
    final AtomicInteger scaleY = new AtomicInteger(-1);
    Button flipHorizontalButton = UIUtils.createSmallButton(VaadinIcon.ARROWS_LONG_H);
    flipHorizontalButton.addClickListener(event -> {
      cropperJs.scaleX(scaleX.get());
      scaleX.set(scaleX.get() * -1);
    });
    Button flipVerticalButton = UIUtils.createSmallButton(VaadinIcon.ARROWS_LONG_V);
    flipVerticalButton.addClickListener(event -> {
      cropperJs.scaleY(scaleY.get());
      scaleY.set(scaleY.get() * -1);
    });

    Button resetButton = UIUtils.createSmallButton(VaadinIcon.REFRESH);
    resetButton.addClickListener(event -> cropperJs.reset());

    toolsLayout
        .add(moveButton, cropButton, zoomOutButton, zoomInButton, moveLeftButton,
            moveRightButton,
            moveUpButton, moveDownButton, rotateLeftButton, rotateRightButton,
            flipHorizontalButton,
            flipVerticalButton,
            resetButton);

    contentLayout.add(toolsLayout);
  }

  @Override
  protected void setPresentationValue(StoredFile newPresentationValue) {
    if (newPresentationValue.getContent() != null) {
      storedFile = newPresentationValue;

      image.setSrc(new StreamResource(newPresentationValue.getFilename(),
          () -> new ByteArrayInputStream(storedFile.getContent())));
    } else {
      storedFile = null;
      image.setSrc(AppConst.NO_PICTURE);
    }
  }
}