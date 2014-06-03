package com.appearnetworks.aiq.integrationframework.impl.platform;

import com.appearnetworks.aiq.integrationframework.platform.MessageAttachment;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import java.io.IOException;

public class AttachmentHttpMessageConverter extends AbstractHttpMessageConverter<MessageAttachment> {

    public AttachmentHttpMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return MessageAttachment.class.isAssignableFrom(clazz);
    }

    @Override
    protected MessageAttachment readInternal(Class<? extends MessageAttachment> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("write-only");
    }

    @Override
    protected MediaType getDefaultContentType(MessageAttachment attachment) {
        return attachment.contentType;
    }

    @Override
    protected Long getContentLength(MessageAttachment attachment, MediaType contentType) throws IOException {
        return (long) attachment.data.length;
    }

    @Override
    protected void writeInternal(MessageAttachment attachment, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        StreamUtils.copy(attachment.data, outputMessage.getBody());
        outputMessage.getBody().flush();
    }
}
