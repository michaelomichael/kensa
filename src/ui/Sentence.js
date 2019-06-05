import React, {Component} from "react";

export class Sentence extends Component {

    constructor(props) {
        super(props);

        this.state = {
            acronyms: this.props.acronyms
        };
    }

    acronymExpansionFor(token) {
        if (token.type === 'Acronym') {
            return this.state.acronyms[token.value];
        }
    }

    // TODO :: decide which version to go for. The below works with array object and the above with JSON array like:
    /*
    "acronyms": {
        "FTTP": "Fibre To The Premises",
        "KCI": "Keep Customer Informed",
        "ONT": "Optical Network Termination"
    },
     */
    // acronymExpansionFor(token) {
    //     if (token.type === 'Acronym') {
    //         let acro = this.state.acronyms.find((entry) => {
    //             return entry.acronym === token.value;
    //         });
    //         if (acro)
    //             return acro.meaning;
    //         return "";
    //     }
    // }


    classFor(token) {
        let c = "token-" + token.type.toLowerCase();

        if (token.type === 'Acronym') {
            c = "tooltip " + c;
        }

        return c;
    }

    render() {
        const sentence = this.props.sentence;
        return (
            <div>
                {sentence.map((token, index) => <span key={index} className={this.classFor(token)}
                                                      data-tooltip={this.acronymExpansionFor(token)}>{token.value}</span>)
                    .reduce((prev, curr) => [prev, ' ', curr])}
            </div>
        );
    }
}