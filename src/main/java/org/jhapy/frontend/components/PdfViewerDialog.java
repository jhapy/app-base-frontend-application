package org.jhapy.frontend.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout.ContentAlignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.server.StreamResource;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.utils.PdfConvert;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.client.BaseServices;
import org.jhapy.frontend.utils.UIUtils;

import java.io.ByteArrayInputStream;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 05/09/2020
 */
public class PdfViewerDialog extends AbstractDialog implements HasLogger {

  private final StoredFile storedFile;
  private FlexBoxLayout contentLayout;
  private PdfViewer pdfViewer;

  public PdfViewerDialog(StoredFile storedFile) {
    this.storedFile = storedFile;
    if (storedFile != null && storedFile.getId() != null) {
      ServiceResult<StoredFile> _storedFile =
          BaseServices.getResourceService().getById(new GetByStrIdQuery(storedFile.getId()));
      if (_storedFile.getIsSuccess() && _storedFile.getData() != null) {
        storedFile.setContent(_storedFile.getData().getContent());
        storedFile.setPdfContent(_storedFile.getData().getPdfContent());
      }
    }
  }

  @Override
  protected String getTitle() {
    return getTranslation("element.global.viewPDF");
  }

  public StoredFile getStoredFile() {
    return storedFile;
  }

  @Override
  protected Component getContent() {
    var loggerPrefix = getLoggerPrefix("getContent");
    contentLayout = new FlexBoxLayout();
    contentLayout.setWidthFull();
    contentLayout.setHeightFull();
    contentLayout.setFlexDirection(FlexDirection.COLUMN);

    if (storedFile != null) {
      byte[] fileContent = storedFile.getContent();
      String filename = storedFile.getFilename();
      if (!storedFile.getMimeType().contains("pdf")) {
        if (!storedFile.getPdfConvertStatus().equals(PdfConvert.CONVERTED)) {
          Button downloadButton =
              UIUtils.createButton(
                  getTranslation(
                      storedFile.getPdfConvertStatus().equals(PdfConvert.NOT_CONVERTED)
                          ? "error.docConvert.notConvertedYet"
                          : "error.docConvert.cannotConvert"),
                  VaadinIcon.DOWNLOAD,
                  ButtonVariant.LUMO_ERROR);
          Anchor downloadLink =
              new Anchor(
                  new StreamResource(
                      storedFile.getFilename(),
                      () -> new ByteArrayInputStream(storedFile.getContent())),
                  "");
          downloadLink.getElement().setAttribute("download", true);
          downloadLink.add(downloadButton);
          contentLayout.setAlignContent(ContentAlignment.CENTER);
          contentLayout.add(downloadLink);
          fileContent = null;
        } else {
          fileContent = storedFile.getPdfContent();
          filename = filename.substring(0, filename.lastIndexOf(".")) + ".pdf";
        }
      }
      if (fileContent != null) {
        byte[] finalFileContent = fileContent;
        pdfViewer =
            new PdfViewer(
                new StreamResource(filename, () -> new ByteArrayInputStream(finalFileContent)));
        pdfViewer.setHeight("100%");
        contentLayout.add(pdfViewer);
      }
    }

    return contentLayout;
  }

  @Override
  protected boolean hasSaveButton() {
    return false;
  }

  @Override
  protected void onDialogResized(DialogResizeEvent event) {}

  @Override
  public boolean canMaximize() {
    return true;
  }
}