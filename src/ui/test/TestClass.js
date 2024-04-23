import React, {useEffect, useState} from "react";
import {stateClassFor} from "../Util";
import Test from "./Test";
import Information from "./information/Information";

const TestClass = () => {
    const [data, setData] = useState(null);
    const [startExpanded, setStartExpanded] = useState(0);

    useEffect(() => {
        let data = JSON.parse(document.querySelector("script[id^='test-result']").textContent);

        if (window.location.hash) {
            setStartExpanded(data.tests.findIndex(test => window.location.hash.substring(1) === test.testMethod))
        } else {
            setStartExpanded(data.tests.findIndex(test => test.state === "Failed"))
        }

        setData(data)
    }, [])

    if (data) {
        return (
            <>
                <section className={"hero " + stateClassFor(data.state)}>
                    <div className="hero-body">
                        <h1 className="title">{data.displayName}</h1>
                    </div>
                </section>
                <section className="section">
                    <Information issues={data.issues} notes={data.notes}/>
                    {
                        data.tests.map((test, idx) =>
                            <Test test={test} key={idx} startExpanded={idx === startExpanded}/>
                        )
                    }

                </section>
            </>
        )
    }
}

export default TestClass