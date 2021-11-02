package com.imagidoc.poleemploi.scanintelligent.web.rest.util;

import org.junit.Assert;
import org.junit.Test;

public class HeaderUtilTest {

	@Test
	public void testExtractIp() {
		Assert.assertEquals("172.18.208.42", HeaderUtil.extractIp("172.18.208.42, 10.10.86.50"));
		Assert.assertEquals("172.18.208.42", HeaderUtil.extractIp("172.18.208.42"));
	}
}
