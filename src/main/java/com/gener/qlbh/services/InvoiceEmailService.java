package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.SendInvoiceEmailReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.Order;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.repositories.OrderRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InvoiceEmailService {

    private final JavaMailSender mailSender;
    private final OrderRepository orderRepository;

    public ResponseEntity<ResponseObject> sendInvoiceEmail(
            Long orderId,
            SendInvoiceEmailReq req,
            MultipartFile pdfFile
    ) throws APIException {
        if (req == null) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Thiếu thông tin gửi email")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }

        String to = req.getTo() == null ? "" : req.getTo().trim();
        if (to.isEmpty()) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Email người nhận không được để trống")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }

        if (pdfFile == null || pdfFile.isEmpty()) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Không tìm thấy file PDF hóa đơn")
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }

        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                APIException.builder()
                        .status(ErrorCode.NOT_FOUND.getCode())
                        .message("Không tìm thấy hóa đơn với id = " + orderId)
                        .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatus())
                        .build()
        );

        String orderCode = order.getCode() != null ? order.getCode() : ("HD-" + orderId);
        String subject = (req.getSubject() == null || req.getSubject().trim().isEmpty())
                ? "Hóa đơn " + orderCode
                : req.getSubject().trim();

        String content = (req.getContent() == null || req.getContent().trim().isEmpty())
                ? buildDefaultContent(orderCode)
                : req.getContent().trim();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);

            String attachmentName = pdfFile.getOriginalFilename();
            if (attachmentName == null || attachmentName.isBlank()) {
                attachmentName = orderCode + ".pdf";
            }

            helper.addAttachment(attachmentName, pdfFile);
            mailSender.send(message);

            return ResponseEntity
                    .status(SuccessCode.REQUEST.getHttpStatusCode())
                    .body(ResponseObject.builder()
                            .status(SuccessCode.REQUEST.getStatus())
                            .message("Gửi hóa đơn qua email thành công")
                            .data(orderCode)
                            .build());

        } catch (MessagingException e) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Không thể gửi email: " + e.getMessage())
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        } catch (Exception e) {
            throw APIException.builder()
                    .status(ErrorCode.BAD_REQUEST.getCode())
                    .message("Gửi email thất bại: " + e.getMessage())
                    .httpStatusCode(ErrorCode.BAD_REQUEST.getHttpStatus())
                    .build();
        }
    }

    private String buildDefaultContent(String orderCode) {
        return """
                Kính gửi Quý khách,

                Em gửi anh/chị hóa đơn %s đính kèm theo email này.

                Trân trọng.
                """.formatted(orderCode);
    }
}