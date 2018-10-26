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
import forms.chatbot.web.constants.FormsChatbotWebWebKeys;

import java.util.Map;
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
			if (ddmFormField.isTransient()) {
				continue;
			}
			
			JSONObject questionJSONObject = jsonFactory.createJSONObject();
			JSONObject answerJSONObject = jsonFactory.createJSONObject();

			LocalizedValue localizedValue = ddmFormField.getLabel();

			String label = localizedValue.getString(ddmForm.getDefaultLocale());

			String questionId = ddmFormField.getName();

			questionJSONObject.put("id", questionId);
			questionJSONObject.put("message", label);

			String answerId = "answer_" + questionId;

			questionJSONObject.put("trigger", answerId);

			answerJSONObject.put("id", answerId);

			if (Objects.equals(ddmFormField.getType(), "checkbox_multiple") ||
				Objects.equals(ddmFormField.getType(), "radio") ||
				Objects.equals(ddmFormField.getType(), "select")) {

				DDMFormFieldOptions ddmFormFieldOptions =
					ddmFormField.getDDMFormFieldOptions();

				JSONArray optionsValuesJSONArray =
					jsonFactory.createJSONArray();

				Map<String, LocalizedValue> options =
					ddmFormFieldOptions.getOptions();

				answerJSONObject.put("options", optionsValuesJSONArray);

				for (String optionValue : options.keySet()) {
					JSONObject optionJSONObject =
						jsonFactory.createJSONObject();

					LocalizedValue optionLocalizedValue = options.get(
						optionValue);

					String optionLabel = optionLocalizedValue.getString(
						ddmForm.getDefaultLocale());

					optionJSONObject.put("label", optionLabel);
					optionJSONObject.put("value", optionValue);

					optionsValuesJSONArray.put(optionJSONObject);
				}
			}
			else {
				answerJSONObject.put("user", true);
			}

			addTrigger(jsonArray, questionJSONObject);

			jsonArray.put(questionJSONObject);
			jsonArray.put(answerJSONObject);
		}

		PortletResponseUtil.write(resourceResponse, jsonArray.toJSONString());
	}

	protected void addTrigger(JSONArray jsonArray, JSONObject currentStep) {
		if (jsonArray.length() > 0) {
			JSONObject previous = jsonArray.getJSONObject(
				jsonArray.length() - 1);

			if (previous.has("options")) {
				JSONArray optionsJSONArray = (JSONArray)previous.get("options");

				for (int i = 0; i < optionsJSONArray.length(); i++) {
					JSONObject optionJSONObject =
						optionsJSONArray.getJSONObject(i);

					optionJSONObject.put("trigger", currentStep.get("id"));
				}
			}
			else {
				previous.put("trigger", currentStep.get("id"));
			}
		}
	}

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		String formInstanceUuid = ParamUtil.getString(
			resourceRequest, "formInstanceUuid",
			FormsChatbotWebWebKeys.FORMS_UUID);

		DDMFormInstance formInstance =
			ddmFormInstanceLocalService.fetchDDMFormInstanceByUuidAndGroupId(
				formInstanceUuid, portal.getScopeGroupId(resourceRequest));

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