/*
 * MIT License
 *
 * Copyright (c) 2020 engineer365.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.engineer365.common.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.engineer365.common.json.JacksonHelper;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.micrometer.MicrometerCapability;
import feign.slf4j.Slf4jLogger;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Configuration
@lombok.Getter
@lombok.Setter
public class FeignConfig {

  @Autowired
  FeignConfigProps props;
  
  @Autowired
  MeterRegistry meterRegistry;

  Feign.Builder builder;

  @PostConstruct
  public void init() {
    if (getProps().isMetricsEnabled()) {
      initMetrics();
    } 

    setBuilder(createBuilder());
  }

  public void initMetrics() {
    if (getMeterRegistry() == null) {
      setMeterRegistry(buildDefaultMeterRegistry());
    }
  }

  public MeterRegistry buildDefaultMeterRegistry() {
    return new SimpleMeterRegistry(SimpleConfig.DEFAULT, Clock.SYSTEM);
  }

  public Feign.Builder createBuilder() {
    var p = getProps();

    var r = Feign.builder();
    r.options(p.getOptions());
    
    r.logger(new Slf4jLogger())
    //.errorDecoder(new MyErrorDecoder())
    .logLevel(p.getLogLevel());

    buildJsonCodec(r);
    
    if (p.isMetricsEnabled()) {
      r.addCapability(new MicrometerCapability(getMeterRegistry()));
    }

    r.requestInterceptor(resolveRequestInterceptor());
    return r;
  }

  public RequestInterceptor resolveRequestInterceptor() {
    return new RequestInterceptor() {
      @Override public void apply(RequestTemplate template) {
        template.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
      }
    };
  }

  public ObjectMapper resolveObjectMapper() {
    return JacksonHelper.buildMapper();
  }

  public Feign.Builder buildJsonCodec(Feign.Builder builder) {
    var mapper =  resolveObjectMapper();

    builder.encoder(new JacksonEncoder(mapper))
           .decoder(new JacksonDecoder(mapper));
    return builder;
  }

  public <T> T build(Class<T> clientClass, String uri) {
    return getBuilder().target(clientClass, uri);
  }

}
