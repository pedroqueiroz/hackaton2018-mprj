import React from 'react';
import ReactDOM from 'react-dom';
import ChatBot from 'react-simple-chatbot';

const {getFormEntriesByUserURL, getFormDefinition, saveFormEntry} = window.chatBotConstants;

console.log(getFormEntriesByUserURL, getFormDefinition, saveFormEntry);

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
		fetch(getFormDefinition).then(response => {
			response.json().then(() => {
				console.log(response, 'getFormDefinition');
			})
		}).catch(error => console.log(error));
	}

	saveFormEntry() {
		fetch(saveFormEntry).then(response => {
			response.json().then(() => {
				console.log(response, 'saveFormEntry');
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