package com.lombardrisk.ignis.server.job.staging.file;

import io.vavr.Function1;
import io.vavr.Function2;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;
import java.net.URL;
import java.util.NoSuchElementException;

public interface ErrorFileLinkOrStream {

    static ErrorFileLinkOrStream fileLink(final URL uri) {
        return new ForwardedDownloadLink(uri);
    }

    static ErrorFileLinkOrStream fileStream(final String fileName, final InputStream inputStream) {
        return new FileNameAndStream(fileName, inputStream);
    }

    default <R> R fold(
            final Function1<URL, R> downloadLinkHandler,
            final Function2<String, InputStream, R> fileNameAndStreamHandler) {

        if (this instanceof ForwardedDownloadLink) {
            ForwardedDownloadLink forwardedDownloadLink = (ForwardedDownloadLink) this;
            return downloadLinkHandler.apply(forwardedDownloadLink.link);
        }

        FileNameAndStream fileNameAndStream = (FileNameAndStream) this;
        return fileNameAndStreamHandler.apply(fileNameAndStream.fileName, fileNameAndStream.inputStream);
    }

    URL getLink();

    FileNameAndStream getFileNameAndStream();

    @Data
    @AllArgsConstructor
    class ForwardedDownloadLink implements ErrorFileLinkOrStream {

        private final URL link;

        @Override
        public FileNameAndStream getFileNameAndStream() {
            throw new NoSuchElementException("GetFileNameAndStream link called on ForwardedDownloadLink");
        }
    }

    @Data
    @AllArgsConstructor
    class FileNameAndStream implements ErrorFileLinkOrStream {

        private final String fileName;
        private final InputStream inputStream;

        @Override
        public URL getLink() {
            throw new NoSuchElementException("Get link called on FileNameAndStream");
        }

        @Override
        public FileNameAndStream getFileNameAndStream() {
            return this;
        }
    }
}
