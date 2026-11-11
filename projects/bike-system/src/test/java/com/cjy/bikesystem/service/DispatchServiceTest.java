package com.cjy.bikesystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatchServiceTest {

    private final DispatchService service = new DispatchService(
            null, null, null, null, new ObjectMapper()
    );

    @Test
    void normalizesCampusLocationToStandardArea() {
        assertEquals("教学区", service.normalizeArea("教学楼A区正门"));
        assertEquals("宿舍区", service.normalizeArea("宿舍区6号楼"));
        assertEquals("图书馆", service.normalizeArea("图书馆北门"));
        assertEquals("食堂区", service.normalizeArea("第二食堂门口"));
        assertEquals("体育馆", service.normalizeArea("体育馆东侧"));
    }
}
