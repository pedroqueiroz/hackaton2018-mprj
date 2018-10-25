import React from 'react';
import ReactDOM from 'react-dom';
import ChatBot from 'react-simple-chatbot';

class App extends React.Component {
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