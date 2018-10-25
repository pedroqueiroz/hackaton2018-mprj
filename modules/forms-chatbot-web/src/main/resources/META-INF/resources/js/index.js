import React from 'react';
import ReactDOM from 'react-dom';
import ChatBot from 'react-simple-chatbot';

const {getFormEntriesByUserURL, getFormDefinitionURL, saveFormEntryURL} = window.chatBotConstants;

console.log(getFormEntriesByUserURL, getFormDefinitionURL, saveFormEntryURL);

class App extends React.Component {
	componentWillMount() {
		this.getFormEntriesByUserURL();
		this.getFormDefinition();
		this.saveFormEntry();
	}

	getFormEntriesByUserURL() {
		fetch(getFormEntriesByUserURL).then(response => {
			response.json().then(() => {
				console.log(response, 'getFormEntriesByUserURL');
			})
		}).catch(error => console.log(error));
	}

	getFormDefinition() {
		fetch(getFormDefinitionURL).then(response => {
			response.json().then(() => {
				console.log(response, 'getFormDefinitionURL');
			})
		}).catch(error => console.log(error));
	}

	saveFormEntry() {
		fetch(saveFormEntryURL).then(response => {
			response.json().then(() => {
				console.log(response, 'saveFormEntryURL');
			})
		}).catch(error => console.log(error));
	}

	render() {
		return (
			<div>
				<ChatBot
					steps={[
						{
							id: 'hello-world',
							message: 'Hello World!',
							end: true,
						}
					]}
					floating
				/>
			</div>
		);
	}
}

export default function(elementId) {
	ReactDOM.render(<App />, document.getElementById(elementId));
}