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

import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceLocalService;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import forms.chatbot.web.constants.FormsChatbotWebPortletKeys;

import java.util.Objects;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rafael Praxedes
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + FormsChatbotWebPortletKeys.FORMS_CHATBOT_WEB,
		"mvc.command.name=getFormDefinition"
	},
	service = MVCResourceCommand.class
)
public class GetFormDefinitionMVCResourceCommand
	extends BaseMVCResourceCommand {

	public void writeResponse(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse,
			DDMForm ddmForm)
		throws Exception {

		JSONArray jsonArray = jsonFactory.createJSONArray();

		for (DDMFormField ddmFormField : ddmForm.getDDMFormFields()) {
			JSONObject jsonObject = jsonFactory.createJSONObject();

			LocalizedValue localizedValue = ddmFormField.getLabel();

			String label = localizedValue.getString(ddmForm.getDefaultLocale());

			jsonObject.put("id", ddmFormField.getName());

			if (Objects.equals(ddmFormField.getType(), "multiple_checkbox") ||
				Objects.equals(ddmFormField.getType(), "radio") ||
				Objects.equals(ddmFormField.getType(), "select")) {

				DDMFormFieldOptions ddmFormFieldOptions =
					ddmFormField.getDDMFormFieldOptions();

				JSONArray optionsValuesJSONArray =
					jsonFactory.createJSONArray();

				jsonObject.put("options", optionsValuesJSONArray);

				for (int i = 0; i < optionsValuesJSONArray.length(); i++) {
					JSONObject optionJSONObject =
						jsonFactory.createJSONObject();

					String optionValue = optionsValuesJSONArray.getString(i);

					LocalizedValue optionLabel =
						ddmFormFieldOptions.getOptionLabels(optionValue);

					optionJSONObject.put(
						"label",
						optionLabel.getString(ddmForm.getDefaultLocale()));

					optionJSONObject.put("value", optionValue);

					addTrigger(jsonArray, optionJSONObject);
				}
			}
			else {
				jsonObject.put("message", label);

				addTrigger(jsonArray, jsonObject);
			}

			jsonArray.put(jsonObject);
		}

		PortletResponseUtil.write(resourceResponse, jsonArray.toJSONString());
	}

	protected void addTrigger(JSONArray jsonArray, JSONObject currentStep) {
		if (jsonArray.length() > 1) {
			JSONObject previous = jsonArray.getJSONObject(jsonArray.length());

			currentStep.put("trigger", previous.get("id"));
		}
	}

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long formInstanceId = ParamUtil.getLong(
			resourceRequest, "formInstanceId");

		DDMFormInstance formInstance =
			ddmFormInstanceLocalService.fetchDDMFormInstance(formInstanceId);

		DDMStructure structure = formInstance.getStructure();

		DDMForm ddmForm = structure.getDDMForm();

		writeResponse(resourceRequest, resourceResponse, ddmForm);
	}

	@Reference
	protected DDMFormInstanceLocalService ddmFormInstanceLocalService;

	@Reference
	protected JSONFactory jsonFactory;

	@Reference
	protected Portal portal;

}