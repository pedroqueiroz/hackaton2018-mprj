import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import { ThemeProvider } from 'styled-components';
import ChatBot from 'react-simple-chatbot';
import PropTypes from 'prop-types';

const {getFormEntriesByUserURL, getFormDefinitionURL, saveFormEntryURL} = window.chatBotConstants;

const customClient = {
	userAvatar: '/image/user_male_portrait?img_id=37927&img_id_token=%2BRnC9iQPv6%2Fk5V0rVChhd5yGVjw%3D&t=1540522935174',
	botAvatar: 'https://www.novaconcursos.com.br/portal/wp-content/uploads/2015/08/MPRJ_logo1.jpg'
}

const theme = {
  background: '#f5f8fb',
  fontFamily: 'Helvetica Neue',
  headerBgColor: '#A82229',
  headerFontColor: '#fff',
  headerFontSize: '16px',
  botBubbleColor: '#A82229',
  botFontColor: '#fff !important',
  userBubbleColor: '#fff !important',
  userFontColor: '#4a4a4a',
};


const STEPS_FAKE = [
	{
		message: "Classificação da Comunicação"
	},
	{
		message: "Identificação"
	},
	{
		message: "CPF"
	},
	{
		message: "Data de Nascimento"
	}
];

const getId = message => {
	if (!message) {
		return 'empty'
	} else {
		return message.replace(/\s/g,'').toLowerCase();
	}
};

class Review extends Component {
	constructor(props) {
		super(props);

		this.state = {
			steps: []
		};
	}

	componentWillMount() {
		const {steps, mapper} = this.props;

		const obj = Object.assign([], steps).filter(result => result);

		const result = mapper.map((result, index) => {
			return {
				question: result.message,
				answer: obj[index].value
			}
		});

		this.setState({ steps: result });
	}

	render() {
		const { steps } = this.state;

		if (steps.length === 0) return;

		return (
			<div style={{ width: '100%' }}>
				<h3>Summary</h3>
				<table>
					<tbody>
						{steps.map(({question, answer}, index) => (
							<tr key={index}>
								<td>{question}</td>
								<td>{answer}</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
		);
	}
}

Review.propTypes = {
	steps: PropTypes.array,
};

Review.defaultProps = {
	steps: undefined,
};

class App extends Component {
	constructor(props) {
		super(props);

		this.state = {
			steps: [],
			opened: false
		}
	}

	componentWillMount() {
		this.getFormEntriesByUserURL();
		this.getFormDefinition();
		// this.saveFormEntry();
	}

	componentDidMount() {
		this.handleEnd = this.handleEnd.bind(this);
		this.toggle = this.toggle.bind(this);
	}

	getFormEntriesByUserURL() {
		fetch(getFormEntriesByUserURL).then(response => {
			response.json().then(result => {
				console.log(result, 'getFormEntriesByUserURL');
			})
		}).catch(error => console.log(error));
	}

	getFormDefinition() {
		fetch(getFormDefinitionURL).then(response => {
			response.json().then(result => {
				console.log(result, 'getFormDefinitionURL');

				let mapper = [];

				mapper.push(
					{
						id: 'hello',
						message: 'Olá, sou seu assistente virtual do MPRJ',
						trigger: 'help'
					},
					{
						id: 'help',
						message: 'Posso te ajudar em algo?',
						trigger: 'options'
					},
					{
						id: 'list',
						component: (
							<div>{'denuncias'}</div>
						),
						trigger: 'help'
					},
					{
						id: 'options',
						options: [
							{ value: 'create', label: 'criar denúncia', trigger: 'create' },
							{ value: 'list', label: 'listar denúncias', trigger: 'list' }
						],
					},
					{
						id: 'review',
						component: <Review mapper={STEPS_FAKE} />,
						asMessage: true,
						trigger: 'update'
					},
					{
						id: 'update',
						message: 'Posso confirmar sua denúncia?',
						trigger: 'update-question'
					},
					{
						id: 'update-question',
						options: [
							{ value: 'sim', label: 'sim', trigger: 'end-message' },
							{ value: 'não', label: 'não', trigger: 'help' },
						]
					},
					{
						id: 'end-message',
						message: 'Denúncia cadastrada com sucesso!',
						trigger: 'thank-you'
					},
					{
						id: 'thank-you',
						message: 'Obrigado!',
						end: true
					}
				)

				// create
				const form = STEPS_FAKE.map(({message}, index, arr) => {
					if (index === arr.length - 1) {
						const question = {
							id: getId(message),
							message,
							trigger: `${index}`,
						}

						const answer = {
							id: `${index}`,
							user: true,
							trigger: 'review'
						}

						return [question, answer];
					}

					const question = {
						id: index === 0 ? 'create' : getId(message),
						message,
						trigger: `${index}`
					}

					const answer = {
						id: `${index}`,
						user: true,
						trigger: getId(arr[index + 1].message)
					}

					return [question, answer];
				}).reduce((a, b) => a.concat(b));

				mapper.push(...form);

				console.log(mapper);

				this.setState({steps: mapper});
			})
		}).catch(error => console.log(error));
	}

	saveFormEntry() {
		fetch(saveFormEntryURL).then(response => {
			response.json().then(result => {
				console.log(result, 'saveFormEntryURL');
			})
		}).catch(error => console.log(error));
	}
	
	handleEnd({renderedSteps, steps, values}) {
		console.log(renderedSteps, steps, values)

		this.setState({opened: false})
	}

	toggle({opened}) {
		this.setState({opened})
	}

	render() {
		const {steps, opened} = this.state;
		const {botAvatar, userAvatar} = customClient;

		console.log('STEPS', steps);

		if (steps.length === 0) return <div/>;

		return (
			<ThemeProvider theme={theme}>
				<ChatBot
					userDelay={500}
					opened={opened}
					handleEnd={this.handleEnd}
					floating
					steps={steps}
					botAvatar={botAvatar}
					userAvatar={userAvatar}
					toggleFloating={this.toggle}
				/>
			</ThemeProvider>
		);
	}
}

export default function(elementId) {
	ReactDOM.render(<App />, document.getElementById(elementId));
}