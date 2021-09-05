package org.jhapy.frontend.components;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.client.BaseServices;
import org.jhapy.frontend.component.cropperjs.CropperConfiguration;
import org.jhapy.frontend.component.cropperjs.CropperJs;
import org.jhapy.frontend.component.cropperjs.model.Data;
import org.jhapy.frontend.component.cropperjs.model.DragMode;
import org.jhapy.frontend.component.cropperjs.model.ViewMode;
import org.jhapy.frontend.utils.UIUtils;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 05/09/2020
 */
public class ImageViewerDialog extends AbstractDialog implements HasLogger {

  private FlexBoxLayout contentLayout;
  private final StoredFile storedFile;
  private final Boolean isReadOnly;
  private CropperJs cropperJs;

  public ImageViewerDialog(StoredFile storedFile, boolean isReadOnly) {
    this.storedFile = storedFile;
    if (storedFile != null && storedFile.getId() != null) {
      ServiceResult<StoredFile> _storedFile = BaseServices
          .getResourceService().getById(new GetByStrIdQuery(storedFile.getId()));
      if (_storedFile.getIsSuccess() && _storedFile.getData() != null) {
        storedFile.setContent(_storedFile.getData().getContent());
        storedFile.setOrginalContent(_storedFile.getData().getOrginalContent());
      }
    }

    this.isReadOnly = isReadOnly;
  }

  @Override
  protected String getTitle() {
    return getTranslation("element.global.viewOrEditImage");
  }

  public StoredFile getStoredFile() {
    return storedFile;
  }

  @Override
  protected Component getContent() {
    contentLayout = new FlexBoxLayout();
    contentLayout.setWidthFull();
    contentLayout.setHeightFull();
    contentLayout.setFlexDirection(FlexDirection.COLUMN);

    if (storedFile != null && !isReadOnly) {
      buildCropper(storedFile.getOrginalContent(), storedFile.getFilename(),
          storedFile != null ? storedFile.getMetadata().get("copperData") : null);
    } else {
      contentLayout.add(new Image(new StreamResource(
          storedFile.getFilename(), () -> new ByteArrayInputStream(storedFile.getContent())),
          storedFile.getFilename()));
    }

    return contentLayout;
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

      imagext.crop(data.getX().intValue(), data.getY().intValue(), data.getWidth().intValue(),
          data.getHeight().intValue());

      storedFile.getMetadata().put("cropperData", data.getJsonString());
      storedFile.setContent(imagext.getByteArray());
      storedFile.setFilename(storedFile.getFilename());
      storedFile.setFilesize((long) storedFile.getContent().length);
      storedFile.setHasChanged(true);

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
    logger().debug(loggerPrefix + "Exposure Time (1 / Shutter Speed): " + exif.get(0x829A));
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
    cropperJs.destroy();
    return true;
  }

  @Override
  protected void onDialogResized(DialogResizeEvent event) {
    cropperJs.resize(event.getHeight(), event.getWidth());
  }

  @Override
  public boolean canMaximize() {
    return false;
  }

  protected void buildCropper(byte[] content, String filename, String cropperData) {
    StreamResource imageResource = new StreamResource(filename,
        () -> new ByteArrayInputStream(content));

    cropperJs = new CropperJs(imageResource);
    cropperJs.setWidthFull();

    CropperConfiguration cropperConfiguration = new CropperConfiguration();
    //cropperConfiguration.setAspectRatio(1f);
    if (StringUtils.isBlank(cropperData)) {
      //cropperConfiguration.setAutoCrop(false);
      cropperConfiguration.setAutoCropArea(1.0f);
    }

    cropperConfiguration.setDragMode(DragMode.CROP);
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
}