package com.airwallex.codechallenge;

import com.airwallex.codechallenge.io.Reader;
import com.airwallex.codechallenge.io.Writer;
import com.airwallex.codechallenge.services.RateProcessorService;

import java.util.Collection;

public class App {

    public static void main(String[] args) {
        ServiceFactory services = ServiceFactory.INSTANCE;
        Reader reader = services.getReader();
        RateProcessorService processor = services.getProcessor();
        Writer writer = services.getWriter();

        reader
                .read(args[0])
                .map(processor::process)
                .flatMap(Collection::parallelStream)
                .forEach(writer::write);
    }
}

