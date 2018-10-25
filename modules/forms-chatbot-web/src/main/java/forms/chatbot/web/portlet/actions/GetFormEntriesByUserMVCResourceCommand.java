/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package forms.chatbot.web.portlet.actions;

import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordLocalService;
import com.liferay.dynamic.data.mapping.util.comparator.DDMFormInstanceRecordModifiedDateComparator;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PrefsParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import forms.chatbot.web.constants.FormsChatbotWebPortletKeys;

/**
 * @author Rafael Praxedes
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + FormsChatbotWebPortletKeys.FormsChatbotWeb,
		"mvc.command.name=getFormEntriesByUser"
	},
	service = MVCResourceCommand.class
)
public class GetFormEntriesByUserMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long formInstanceId = PrefsParamUtil.getLong(
			resourceRequest.getPreferences(), resourceRequest,
			"formInstanceId");

		long userId = portal.getUserId(resourceRequest);
		
		List<DDMFormInstanceRecord> formInstanceRecords =
			ddmFormInstanceRecordLocalService.getFormInstanceRecords(
				formInstanceId, userId, -1, -1,
				new DDMFormInstanceRecordModifiedDateComparator());
		
		writeResponse(resourceRequest, resourceResponse, formInstanceRecords);
	}
	
	public void writeResponse(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse,
			List<DDMFormInstanceRecord> formInstanceRecords)
		throws Exception {

		JSONArray jsonArray = jsonFactory.createJSONArray();
		
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		
		User user = themeDisplay.getUser();

		for (DDMFormInstanceRecord formInstanceRecord : formInstanceRecords) {
			JSONObject jsonObject = jsonFactory.createJSONObject();
			
			jsonObject.put("id", formInstanceRecord.getFormInstanceRecordId());
			jsonObject.put("status", formInstanceRecord.getStatus());
			jsonObject.put("statusLabel", formInstanceRecord.getStatus());
			jsonObject.put(
				"createDate",
				formatDate(
					formInstanceRecord.getCreateDate(), user.getLocale(),
					user.getTimeZoneId()));

			jsonArray.put(jsonObject);
		}

		PortletResponseUtil.write(resourceResponse, jsonArray.toJSONString());
	}
	
	public String formatDate(Date date, Locale locale, String timezoneId) {
		DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofLocalizedDateTime(
				FormatStyle.MEDIUM, FormatStyle.SHORT);

		dateTimeFormatter = dateTimeFormatter.withLocale(locale);

		LocalDateTime localDateTime = LocalDateTime.ofInstant(
			date.toInstant(), ZoneId.of(timezoneId));

		return dateTimeFormatter.format(localDateTime);
	}
	
	@Reference
	protected JSONFactory jsonFactory;

	@Reference
	protected Portal portal;

	@Reference
	protected DDMFormInstanceRecordLocalService ddmFormInstanceRecordLocalService;

}
